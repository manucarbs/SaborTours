package edu.sv.ues.dam235.sabortours.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Convierte una foto elegida por el usuario en un texto data URI pequeño. */
public class ImagenUtil {

    public interface Resultado {
        void ok(String dataUri, int bytes);
        void error(String mensaje);
    }

    // Hilo propio: así no se bloquean las escrituras de Room mientras se comprime
    private static final ExecutorService TRABAJO = Executors.newSingleThreadExecutor();

    public static void procesar(Context context, Uri uri, Resultado cb) {
        Context app = context.getApplicationContext();
        Handler main = new Handler(Looper.getMainLooper());
        TRABAJO.execute(() -> {
            try {
                Bitmap bmp = decodificar(app, uri);
                byte[] jpeg = comprimir(bmp);
                String texto = LimitesFoto.PREFIJO + Base64.encodeToString(jpeg, Base64.NO_WRAP);
                main.post(() -> cb.ok(texto, jpeg.length));
            } catch (IOException e) {
                String m = e.getMessage() == null ? "No se pudo leer la foto" : e.getMessage();
                main.post(() -> cb.error(m));
            } catch (Exception | OutOfMemoryError e) {
                main.post(() -> cb.error("No se pudo procesar la foto"));
            }
        });
    }

    private static Bitmap decodificar(Context app, Uri uri) throws IOException {
        ContentResolver cr = app.getContentResolver();

        // 1. Solo leer las dimensiones, sin cargar la foto completa en memoria
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        try (InputStream in = cr.openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, op);
        }
        if (op.outWidth <= 0 || op.outHeight <= 0) throw new IOException("El archivo no es una imagen válida");

        // 2. Cargarla ya reducida (potencias de 2) sin quedar por debajo del lado máximo
        int muestreo = 1;
        while (Math.max(op.outWidth, op.outHeight) / (muestreo * 2) >= LimitesFoto.LADO_MAX) muestreo *= 2;
        op.inJustDecodeBounds = false;
        op.inSampleSize = muestreo;
        Bitmap bmp;
        try (InputStream in = cr.openInputStream(uri)) {
            bmp = BitmapFactory.decodeStream(in, null, op);
        }
        if (bmp == null) throw new IOException("No se pudo leer la imagen");

        // 3. Ajuste fino al lado máximo y corrección de rotación EXIF
        int rotacion = rotacion(cr, uri);
        float escala = Math.min(1f, (float) LimitesFoto.LADO_MAX / Math.max(bmp.getWidth(), bmp.getHeight()));
        if (rotacion != 0 || escala < 1f) {
            Matrix m = new Matrix();
            m.postRotate(rotacion);
            m.postScale(escala, escala);
            Bitmap nuevo = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
            if (nuevo != bmp) bmp.recycle();
            bmp = nuevo;
        }

        // 4. JPEG no tiene transparencia: se pinta sobre blanco para evitar fondos negros
        if (bmp.hasAlpha()) {
            Bitmap fondo = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(fondo);
            c.drawColor(Color.WHITE);
            c.drawBitmap(bmp, 0, 0, null);
            bmp.recycle();
            bmp = fondo;
        }
        return bmp;
    }

    private static int rotacion(ContentResolver cr, Uri uri) {
        try (InputStream in = cr.openInputStream(uri)) {
            int o = new ExifInterface(in).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (o) {
                case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                default: return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    // Baja la calidad hasta que quepa; si no alcanza, reduce las dimensiones y reintenta
    private static byte[] comprimir(Bitmap original) throws IOException {
        Bitmap actual = original;
        for (int intento = 0; intento < 4; intento++) {
            for (int calidad = 80; calidad >= 40; calidad -= 10) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                actual.compress(Bitmap.CompressFormat.JPEG, calidad, out);
                if (out.size() <= LimitesFoto.BYTES_MAX) return out.toByteArray();
            }
            actual = Bitmap.createScaledBitmap(actual,
                    Math.max(1, (int) (actual.getWidth() * 0.8)),
                    Math.max(1, (int) (actual.getHeight() * 0.8)), true);
        }
        throw new IOException("No se pudo reducir la foto lo suficiente. Prueba con otra.");
    }
}