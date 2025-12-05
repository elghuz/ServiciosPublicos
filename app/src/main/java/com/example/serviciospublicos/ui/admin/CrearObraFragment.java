package com.example.serviciospublicos.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import com.example.serviciospublicos.R;
import com.example.serviciospublicos.firebase.FirestoreService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CrearObraFragment extends Fragment implements OnMapReadyCallback {

    private EditText inputNombre, inputDescripcion, inputRadio;
    private Spinner spinnerSupervisor;
    private RadioGroup radioGroupTipo;
    private RadioButton radioPunto, radioRadio, radioPoligono;
    private Button btnGuardar;
    private ProgressBar progressBar;

    private final FirestoreService firestore = FirestoreService.getInstance();

    // Map
    private GoogleMap googleMap;
    private Marker marker;
    private Circle circle;
    private Polygon polygon;
    private final List<LatLng> polygonPoints = new ArrayList<>();
    private LatLng selectedCenter; // para punto / radio

    // Supervisores
    private final List<String> supervisorNames = new ArrayList<>();
    private final List<String> supervisorUids = new ArrayList<>();

    public CrearObraFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crear_obra, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputNombre = view.findViewById(R.id.inputNombreObra);
        inputDescripcion = view.findViewById(R.id.inputDescripcionObra);
        inputRadio = view.findViewById(R.id.inputRadio);
        spinnerSupervisor = view.findViewById(R.id.spinnerSupervisor);
        radioGroupTipo = view.findViewById(R.id.radioGroupTipoUbicacion);
        radioPunto = view.findViewById(R.id.radioPunto);
        radioRadio = view.findViewById(R.id.radioRadio);
        radioPoligono = view.findViewById(R.id.radioPoligono);
        btnGuardar = view.findViewById(R.id.btnGuardarObra);
        // progressBar = view.findViewById(R.id.progressBarCrearObra); // si la agregas al layout

        // Marca "Punto" por defecto
        radioPunto.setChecked(true);

        // Cargar mapa dentro de mapContainer
        initMap();

        // Cargar supervisores desde Firestore
        cargarSupervisores();

        btnGuardar.setOnClickListener(v -> guardarObra());
    }

    // ------------------ MAPA ------------------

    private void initMap() {
        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.mapContainer, mapFragment);
        transaction.commit();

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        googleMap = gMap;

        // Mover cámara a una posición base (ej. CDMX)
        LatLng base = new LatLng(19.4326, -99.1332);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(base, 12f));

        googleMap.setOnMapClickListener(latLng -> {
            int checkedId = radioGroupTipo.getCheckedRadioButtonId();

            if (checkedId == R.id.radioPoligono) {
                // Añadir vértice al polígono
                polygonPoints.add(latLng);
                redrawPolygon();
            } else {
                // Punto o radio → un solo centro
                selectedCenter = latLng;
                polygonPoints.clear();
                redrawPolygon();

                if (marker != null) marker.remove();
                marker = googleMap.addMarker(new MarkerOptions().position(latLng));

                if (checkedId == R.id.radioRadio) {
                    redrawCircle();
                } else {
                    // Punto simple
                    if (circle != null) {
                        circle.remove();
                        circle = null;
                    }
                }
            }
        });
    }

    private void redrawCircle() {
        if (googleMap == null || selectedCenter == null) return;

        if (circle != null) {
            circle.remove();
        }

        double radiusMeters = 0.0;
        String radioStr = inputRadio.getText().toString().trim();
        if (!radioStr.isEmpty()) {
            try {
                radiusMeters = Double.parseDouble(radioStr);
            } catch (NumberFormatException ignored) { }
        }

        if (radiusMeters <= 0) return;

        circle = googleMap.addCircle(new CircleOptions()
                .center(selectedCenter)
                .radius(radiusMeters));
    }

    private void redrawPolygon() {
        if (googleMap == null) return;

        if (polygon != null) {
            polygon.remove();
        }

        if (polygonPoints.size() >= 3) {
            polygon = googleMap.addPolygon(new PolygonOptions()
                    .addAll(polygonPoints));
        }
    }

    // ------------------ SUPERVISORES ------------------

    private void cargarSupervisores() {
        firestore.getSupervisores()
                .addOnSuccessListener(this::llenarSpinnerSupervisores)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar supervisores", Toast.LENGTH_SHORT).show()
                );
    }

    private void llenarSpinnerSupervisores(QuerySnapshot snapshot) {
        supervisorNames.clear();
        supervisorUids.clear();

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            String nombre = doc.getString("nombre");
            String correo = doc.getString("correo");
            String uid = doc.getId();

            supervisorUids.add(uid);
            supervisorNames.add(nombre + " (" + correo + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                supervisorNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSupervisor.setAdapter(adapter);
    }

    // ------------------ GUARDAR OBRA ------------------

    private void guardarObra() {
        String nombre = inputNombre.getText().toString().trim();
        String descripcion = inputDescripcion.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(getContext(), "Nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerSupervisor.getSelectedItemPosition() == AdapterView.INVALID_POSITION
                || supervisorUids.isEmpty()) {
            Toast.makeText(getContext(), "Selecciona un supervisor", Toast.LENGTH_SHORT).show();
            return;
        }

        String supervisorUid = supervisorUids.get(spinnerSupervisor.getSelectedItemPosition());

        int checkedId = radioGroupTipo.getCheckedRadioButtonId();
        String ubicacionTipo;

        if (checkedId == R.id.radioPoligono) {
            ubicacionTipo = "poligono";
            if (polygonPoints.size() < 3) {
                Toast.makeText(getContext(), "Selecciona al menos 3 puntos en el mapa para el polígono", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (checkedId == R.id.radioRadio) {
            ubicacionTipo = "radio";
            if (selectedCenter == null) {
                Toast.makeText(getContext(), "Haz clic en el mapa para seleccionar el centro", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(inputRadio.getText().toString().trim())) {
                Toast.makeText(getContext(), "Ingresa un radio en metros", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            ubicacionTipo = "punto";
            if (selectedCenter == null) {
                Toast.makeText(getContext(), "Haz clic en el mapa para seleccionar la ubicación", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Double lat = null;
        Double lng = null;
        Double radioMetros = null;
        List<GeoPoint> poligonoGeo = null;

        if ("poligono".equals(ubicacionTipo)) {
            poligonoGeo = new ArrayList<>();
            for (LatLng p : polygonPoints) {
                poligonoGeo.add(new GeoPoint(p.latitude, p.longitude));
            }
        } else {
            if (selectedCenter != null) {
                lat = selectedCenter.latitude;
                lng = selectedCenter.longitude;
            }
            if ("radio".equals(ubicacionTipo)) {
                try {
                    radioMetros = Double.parseDouble(inputRadio.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Radio inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        firestore.createObra(
                nombre,
                descripcion,
                "INICIANDO",
                supervisorUid,
                ubicacionTipo,
                lat,
                lng,
                radioMetros,
                poligonoGeo
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Obra creada correctamente", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error al crear obra", Toast.LENGTH_SHORT).show();
        });
    }
}
