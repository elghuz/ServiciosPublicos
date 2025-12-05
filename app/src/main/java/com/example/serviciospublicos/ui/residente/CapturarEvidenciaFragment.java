package com.example.serviciospublicos.ui.residente;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.serviciospublicos.R;
import com.example.serviciospublicos.auth.AuthService;
import com.example.serviciospublicos.firebase.FirestoreService;
import com.example.serviciospublicos.firebase.StorageService;
import com.example.serviciospublicos.utils.GeometryUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class CapturarEvidenciaFragment extends Fragment {

    private static final int REQ_CAMERA_PERMISSION = 1001;
    private static final int REQ_LOCATION_PERMISSION = 1002;

    private TextView txtNombreObra, txtInfoUbicacion;
    private EditText inputDescripcion, inputEtapa;
    private ImageView imagePreview;
    private TextView txtVideoSeleccionado;
    private ProgressBar progressBar;
    private Button btnTomarFoto, btnGrabarVideo;

    private final FirestoreService firestore = FirestoreService.getInstance();
    private final StorageService storageService = StorageService.getInstance();
    private final AuthService auth = AuthService.getInstance();

    private FusedLocationProviderClient fusedLocationClient;

    private String obraId;
    private String obraNombre;
    private String ubicacionTipo;
    private Double obraLat;
    private Double obraLng;
    private Double radioMetros;
    private List<GeoPoint> poligono;

    private double currentLat;
    private double currentLng;

    private Bitmap capturedBitmap = null;
    private Uri capturedVideoUri = null;

    // Launchers para cámara
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> recordVideoLauncher;

    public CapturarEvidenciaFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_capturar_evidencia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtNombreObra = view.findViewById(R.id.txtNombreObraEvidencia);
        txtInfoUbicacion = view.findViewById(R.id.txtInfoUbicacionObra);
        inputDescripcion = view.findViewById(R.id.inputDescripcionEvidencia);
        inputEtapa = view.findViewById(R.id.inputEtapaConstruccion);
        imagePreview = view.findViewById(R.id.imagePreview);
        txtVideoSeleccionado = view.findViewById(R.id.txtVideoSeleccionado);
        progressBar = view.findViewById(R.id.progressBarEvidencia);
        btnTomarFoto = view.findViewById(R.id.btnTomarFoto);
        btnGrabarVideo = view.findViewById(R.id.btnGrabarVideo);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Recibir obraId desde argumentos
        if (getArguments() != null) {
            obraId = getArguments().getString("obraId");
        }

        if (obraId == null) {
            Toast.makeText(getContext(), "No se recibió la obra", Toast.LENGTH_SHORT).show();
            return;
        }

        initActivityResultLaunchers();
        cargarDatosObra();

        btnTomarFoto.setOnClickListener(v -> checkPermissionsAndCapturePhoto());
        btnGrabarVideo.setOnClickListener(v -> checkPermissionsAndRecordVideo());
    }

    private void initActivityResultLaunchers() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data"); // thumbnail
                            if (imageBitmap != null) {
                                capturedBitmap = imageBitmap;
                                capturedVideoUri = null;
                                imagePreview.setImageBitmap(imageBitmap);
                                imagePreview.setVisibility(View.VISIBLE);
                                txtVideoSeleccionado.setVisibility(View.GONE);
                                subirEvidencia("foto");
                            }
                        }
                    }
                }
        );

        recordVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri videoUri = result.getData().getData();
                        if (videoUri != null) {
                            capturedVideoUri = videoUri;
                            capturedBitmap = null;
                            imagePreview.setVisibility(View.GONE);
                            txtVideoSeleccionado.setText("Video capturado listo para subir");
                            txtVideoSeleccionado.setVisibility(View.VISIBLE);
                            subirEvidencia("video");
                        }
                    }
                }
        );
    }

    // ---------- Cargar obra ----------

    private void cargarDatosObra() {
        firestore.getObrasCollection()
                .document(obraId)
                .get()
                .addOnSuccessListener(this::onObraLoaded)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar obra", Toast.LENGTH_SHORT).show()
                );
    }

    private void onObraLoaded(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(getContext(), "La obra no existe", Toast.LENGTH_SHORT).show();
            return;
        }

        obraNombre = doc.getString("nombre");
        ubicacionTipo = doc.getString("ubicacionTipo");
        obraLat = doc.getDouble("lat");
        obraLng = doc.getDouble("lng");
        radioMetros = doc.getDouble("radioMetros");

        List<GeoPoint> polyTmp = (List<GeoPoint>) doc.get("poligono");
        if (polyTmp != null) {
            poligono = new ArrayList<>(polyTmp);
        }

        txtNombreObra.setText("Obra: " + obraNombre);

        String info = "Ubicación tipo: " + ubicacionTipo;
        if ("punto".equals(ubicacionTipo) || "radio".equals(ubicacionTipo)) {
            info += String.format("\nLat: %.5f, Lng: %.5f", obraLat, obraLng);
        }
        if ("radio".equals(ubicacionTipo) && radioMetros != null) {
            info += "\nRadio: " + radioMetros + " m";
        }
        if ("poligono".equals(ubicacionTipo)) {
            info += "\nPolígono con " + (poligono != null ? poligono.size() : 0) + " vértices.";
        }
        txtInfoUbicacion.setText(info);
    }

    // ---------- Permisos y captura ----------

    private void checkPermissionsAndCapturePhoto() {
        if (!checkCameraPermission()) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
            return;
        }
        if (!checkLocationPermission()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERMISSION);
            return;
        }
        obtenerUbicacionYTomarFoto();
    }

    private void checkPermissionsAndRecordVideo() {
        if (!checkCameraPermission()) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
            return;
        }
        if (!checkLocationPermission()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERMISSION);
            return;
        }
        obtenerUbicacionYGrabarVideo();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CAMERA_PERMISSION || requestCode == REQ_LOCATION_PERMISSION) {
            // El usuario tendrá que volver a presionar el botón
            Toast.makeText(getContext(), "Permiso otorgado, intenta de nuevo", Toast.LENGTH_SHORT).show();
        }
    }

    private void obtenerUbicacionYTomarFoto() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(getContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();

                    if (!estaDentroDeObra(currentLat, currentLng)) {
                        Toast.makeText(getContext(), "No estás dentro del área de la obra", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePictureLauncher.launch(intent);
                });
    }

    private void obtenerUbicacionYGrabarVideo() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(getContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();

                    if (!estaDentroDeObra(currentLat, currentLng)) {
                        Toast.makeText(getContext(), "No estás dentro del área de la obra", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    recordVideoLauncher.launch(intent);
                });
    }

    // ---------- Validar dentro de la obra ----------

    private boolean estaDentroDeObra(double lat, double lng) {
        if (ubicacionTipo == null) return false;

        if ("punto".equals(ubicacionTipo)) {
            if (obraLat == null || obraLng == null) return false;
            // tolerancia 50m alrededor del punto
            double d = GeometryUtils.distanceMeters(lat, lng, obraLat, obraLng);
            return d <= 50.0;
        }

        if ("radio".equals(ubicacionTipo)) {
            if (obraLat == null || obraLng == null || radioMetros == null) return false;
            double d = GeometryUtils.distanceMeters(lat, lng, obraLat, obraLng);
            return d <= radioMetros;
        }

        if ("poligono".equals(ubicacionTipo)) {
            if (poligono == null || poligono.isEmpty()) return false;
            return GeometryUtils.isPointInPolygon(lat, lng, poligono);
        }

        return false;
    }

    // ---------- Subir evidencia ----------

    private void subirEvidencia(String tipo) {
        String uid = auth.getCurrentUid();
        if (uid == null) {
            Toast.makeText(getContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String descripcion = inputDescripcion.getText().toString().trim();
        String etapa = inputEtapa.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        Task<Uri> uploadTask;

        if ("foto".equals(tipo) && capturedBitmap != null) {
            uploadTask = storageService.uploadImageEvidencia(obraId, uid, capturedBitmap);
        } else if ("video".equals(tipo) && capturedVideoUri != null) {
            uploadTask = storageService.uploadVideoEvidencia(obraId, uid, capturedVideoUri);
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No hay archivo para subir", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadTask
                .addOnSuccessListener(downloadUri -> {
                    String urlArchivo = downloadUri.toString();
                    registrarEvidenciaEnFirestore(uid, tipo, urlArchivo, descripcion, etapa);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al subir archivo", Toast.LENGTH_SHORT).show();
                });
    }

    private void registrarEvidenciaEnFirestore(String uid,
                                               String tipo,
                                               String urlArchivo,
                                               String descripcion,
                                               String etapa) {

        // Por ahora usamos el correo como nombre simple
        firestore.getUsuario(uid).addOnSuccessListener(userDoc -> {
            String nombreUsuario = userDoc.getString("nombre");
            String rolUsuario = userDoc.getString("rol");

            firestore.addEvidencia(
                    obraId,
                    uid,
                    nombreUsuario != null ? nombreUsuario : uid,
                    rolUsuario != null ? rolUsuario : "OPERADOR",
                    tipo,
                    urlArchivo,
                    descripcion,
                    etapa,
                    currentLat,
                    currentLng
            ).addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Evidencia registrada", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error al registrar evidencia", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
