package edu.sv.ues.dam235.sabortours.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Restaurante;
import edu.sv.ues.dam235.sabortours.data.local.Usuario;
import edu.sv.ues.dam235.sabortours.data.repository.ReservaRepository;
import edu.sv.ues.dam235.sabortours.data.repository.RestauranteRepository;
import edu.sv.ues.dam235.sabortours.util.Formatos;
import edu.sv.ues.dam235.sabortours.util.Franjas;

public class ReservarActivity extends AppCompatActivity {

    private String restauranteId, restauranteNombre;
    private Restaurante restaurante;                 // viene de Room
    private int personas = 2;
    private String fechaIso = null;
    private String horaSel = null;
    private List<String> franjas = new ArrayList<>();
    private Map<String, Long> ocupados = null;       // "19:00" -> personas ya reservadas
    private boolean consultando = false;

    private TextView tvPersonas, tvDisponibilidad;
    private EditText etFecha;
    private ChipGroup chipHoras;
    private ReservaRepository reservaRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservar);

        restauranteId = getIntent().getStringExtra("restaurante_id");
        restauranteNombre = getIntent().getStringExtra("restaurante_nombre");
        if (restauranteId == null) { finish(); return; }

        tvPersonas = findViewById(R.id.tvPersonas);
        tvDisponibilidad = findViewById(R.id.tvDisponibilidad);
        etFecha = findViewById(R.id.etFecha);
        chipHoras = findViewById(R.id.chipHoras);
        Button btnMenos = findViewById(R.id.btnMenos);
        Button btnMas = findViewById(R.id.btnMas);
        MaterialButton btnConfirmar = findViewById(R.id.btnConfirmarReserva);

        reservaRepo = new ReservaRepository(getApplicationContext());

        // El horario y la capacidad vienen de Room
        new RestauranteRepository(this).restaurante(restauranteId).observe(this, r -> {
            if (r == null) return;
            boolean primeraVez = restaurante == null;
            restaurante = r;
            if (fechaIso == null) return;
            if (primeraVez) cargarDia(); else renderFranjas();
        });

        etFecha.setFocusable(false);   // solo se elige con el calendario
        etFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialogo = new DatePickerDialog(this, (view, y, m, d) -> {
                fechaIso = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                etFecha.setText(Formatos.fechaBonita(fechaIso));
                cargarDia();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dialogo.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialogo.show();
        });

        btnMenos.setOnClickListener(v -> { if (personas > 1) { personas--; cambioPersonas(); } });
        btnMas.setOnClickListener(v -> { if (personas < 10) { personas++; cambioPersonas(); } });

        btnConfirmar.setOnClickListener(v -> {
            if (fechaIso == null) {
                Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show();
                return;
            }
            if (horaSel == null) {
                Toast.makeText(this, "Elige una hora", Toast.LENGTH_SHORT).show();
                return;
            }
            String hora = horaSel;
            btnConfirmar.setEnabled(false);      // evita reservar dos veces con un doble toque

            Context app = getApplicationContext();
            String uid = FirebaseAuth.getInstance().getUid();
            AppDatabase.io.execute(() -> {
                Usuario u = AppDatabase.get(app).usuarioDao().buscarPorId(uid);
                String nombre = u == null ? "Cliente" : u.nombre;
                runOnUiThread(() -> reservaRepo.crear(
                        restauranteId, restauranteNombre, nombre, fechaIso, hora, personas,
                        new ReservaRepository.Resultado() {
                            @Override public void ok() {
                                Toast.makeText(app, "Reserva enviada a " + restauranteNombre, Toast.LENGTH_LONG).show();
                                finish();
                            }
                            @Override public void error(String mensaje) {
                                Toast.makeText(app, mensaje, Toast.LENGTH_LONG).show();
                                btnConfirmar.setEnabled(true);
                                cargarDia();     // la disponibilidad pudo cambiar: se vuelve a consultar
                            }
                        }));
            });
        });
    }

    private void cambioPersonas() {
        tvPersonas.setText(personas + (personas == 1 ? " persona" : " personas"));
        if (restaurante != null && fechaIso != null) renderFranjas();
    }

    // Se llama al elegir fecha: calcula las franjas y consulta cuánto cupo queda en cada una
    private void cargarDia() {
        horaSel = null;
        ocupados = null;
        if (restaurante == null) {
            tvDisponibilidad.setText("Cargando información del restaurante...");
            return;
        }
        franjas = Franjas.generar(restaurante.apertura, restaurante.cierre, fechaIso);
        consultando = !franjas.isEmpty();
        renderFranjas();
        if (franjas.isEmpty()) return;

        String fechaConsultada = fechaIso;
        reservaRepo.cuposDelDia(restauranteId, fechaConsultada, mapa -> {
            if (!fechaConsultada.equals(fechaIso)) return;   // el usuario cambió de fecha mientras tanto
            consultando = false;
            ocupados = mapa;                                  // null si no hubo conexión
            renderFranjas();
        });
    }

    private long libres(String hora) {
        long ocupado = ocupados == null || !ocupados.containsKey(hora) ? 0 : ocupados.get(hora);
        return restaurante.capacidad - ocupado;
    }

    private void renderFranjas() {
        chipHoras.setOnCheckedStateChangeListener(null);
        chipHoras.removeAllViews();

        if (fechaIso == null) { tvDisponibilidad.setText("Elige primero una fecha"); return; }
        if (franjas.isEmpty()) {
            tvDisponibilidad.setText("No hay horarios disponibles para esta fecha");
            return;
        }

        for (String hora : franjas) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_chip_hora, chipHoras, false);
            chip.setId(View.generateViewId());
            chip.setText(Formatos.hora12(hora));
            chip.setTag(hora);
            // Sin datos de cupo, todas quedan habilitadas: la transacción verifica al confirmar
            chip.setEnabled(ocupados == null || libres(hora) >= personas);
            chipHoras.addView(chip);
            if (hora.equals(horaSel)) {
                if (chip.isEnabled()) chip.setChecked(true); else horaSel = null;
            }
        }

        chipHoras.setOnCheckedStateChangeListener((grupo, ids) -> {
            horaSel = ids.isEmpty() ? null : (String) grupo.findViewById(ids.get(0)).getTag();
            actualizarTexto();
        });
        actualizarTexto();
    }

    private void actualizarTexto() {
        if (consultando) { tvDisponibilidad.setText("Consultando disponibilidad..."); return; }
        if (ocupados == null) {
            tvDisponibilidad.setText("No se pudo consultar la disponibilidad; se verificará al confirmar");
            return;
        }
        if (horaSel == null) { tvDisponibilidad.setText("Elige una hora"); return; }
        long l = libres(horaSel);
        tvDisponibilidad.setText("Quedan " + l + (l == 1 ? " lugar" : " lugares")
                + " a las " + Formatos.hora12(horaSel));
    }
}