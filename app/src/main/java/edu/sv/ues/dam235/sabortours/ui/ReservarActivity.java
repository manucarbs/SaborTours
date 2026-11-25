package edu.sv.ues.dam235.sabortours.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.util.Calendar;
import java.util.Locale;
import edu.sv.ues.dam235.sabortours.R;
import edu.sv.ues.dam235.sabortours.data.local.AppDatabase;
import edu.sv.ues.dam235.sabortours.data.local.Reserva;
import edu.sv.ues.dam235.sabortours.util.Formatos;

public class ReservarActivity extends AppCompatActivity {

    private static final String[] HORAS_TEXTO = {"6:30 PM", "7:00 PM", "7:30 PM", "8:00 PM"};
    private static final String[] HORAS_VALOR = {"18:30", "19:00", "19:30", "20:00"};

    private int personas = 2;
    private String fechaIso = null;
    private TextView tvPersonas;
    private EditText etFecha;
    private Spinner spinnerHora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservar);

        int restauranteId = getIntent().getIntExtra("restaurante_id", 0);
        String restauranteNombre = getIntent().getStringExtra("restaurante_nombre");

        tvPersonas = findViewById(R.id.tvPersonas);
        etFecha = findViewById(R.id.etFecha);
        spinnerHora = findViewById(R.id.spinnerHora);
        Button btnMenos = findViewById(R.id.btnMenos);
        Button btnMas = findViewById(R.id.btnMas);
        MaterialButton btnConfirmar = findViewById(R.id.btnConfirmarReserva);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, HORAS_TEXTO);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHora.setAdapter(adapter);
        spinnerHora.setSelection(1); // 7:00 PM por defecto

        etFecha.setFocusable(false); // solo se elige con el calendario, no se escribe
        etFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialogo = new DatePickerDialog(this, (view, y, m, d) -> {
                fechaIso = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                etFecha.setText(Formatos.fechaBonita(fechaIso));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dialogo.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialogo.show();
        });

        btnMenos.setOnClickListener(v -> { if (personas > 1) personas--; tvPersonas.setText(personas + " personas"); });
        btnMas.setOnClickListener(v -> { if (personas < 10) personas++; tvPersonas.setText(personas + " personas"); });

        btnConfirmar.setOnClickListener(v -> {
            if (fechaIso == null) {
                Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show();
                return;
            }

            Reserva r = new Reserva();
            r.restauranteId = restauranteId;
            r.restauranteNombre = restauranteNombre;
            r.fecha = fechaIso;
            r.hora = HORAS_VALOR[spinnerHora.getSelectedItemPosition()];
            r.personas = personas;
            r.estado = "pendiente";

            // Room no permite escribir en el hilo principal
            AppDatabase.io.execute(() -> {
                AppDatabase.get(getApplicationContext()).reservaDao().insertar(r);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Reserva enviada a " + restauranteNombre, Toast.LENGTH_LONG).show();
                    finish();
                });
            });
        });
    }
}