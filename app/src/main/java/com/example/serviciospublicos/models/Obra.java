package com.example.serviciospublicos.models;

import com.example.serviciospublicos.models.enums.EstatusObra;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Obra {

    private String id;
    private String nombre;
    private String descripcion;
    private EstatusObra estatus;
    private String supervisorAsignado;   // uid del supervisor

    // Ubicación
    private String ubicacionTipo;        // "punto" | "radio" | "poligono"
    private Double lat;
    private Double lng;
    private Double radioMetros;
    private List<GeoPoint> poligono;     // lista de puntos si es polígono

    @ServerTimestamp
    private Date fechaInicio;

    private Date fechaFinEstimada;

    @ServerTimestamp
    private Date fechaCreacion;

    private String creadoPor;            // uid admin

    public Obra() { }

    public Obra(String id,
                String nombre,
                String descripcion,
                EstatusObra estatus,
                String supervisorAsignado,
                String ubicacionTipo,
                Double lat,
                Double lng,
                Double radioMetros,
                List<GeoPoint> poligono,
                Date fechaFinEstimada,
                String creadoPor) {

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estatus = estatus;
        this.supervisorAsignado = supervisorAsignado;
        this.ubicacionTipo = ubicacionTipo;
        this.lat = lat;
        this.lng = lng;
        this.radioMetros = radioMetros;
        this.poligono = poligono;
        this.fechaFinEstimada = fechaFinEstimada;
        this.creadoPor = creadoPor;
    }

    // ----- Getters y Setters -----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstatusObra getEstatus() {
        return estatus;
    }

    public void setEstatus(EstatusObra estatus) {
        this.estatus = estatus;
    }

    public String getSupervisorAsignado() {
        return supervisorAsignado;
    }

    public void setSupervisorAsignado(String supervisorAsignado) {
        this.supervisorAsignado = supervisorAsignado;
    }

    public String getUbicacionTipo() {
        return ubicacionTipo;
    }

    public void setUbicacionTipo(String ubicacionTipo) {
        this.ubicacionTipo = ubicacionTipo;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getRadioMetros() {
        return radioMetros;
    }

    public void setRadioMetros(Double radioMetros) {
        this.radioMetros = radioMetros;
    }

    public List<GeoPoint> getPoligono() {
        return poligono;
    }

    public void setPoligono(List<GeoPoint> poligono) {
        this.poligono = poligono;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFinEstimada() {
        return fechaFinEstimada;
    }

    public void setFechaFinEstimada(Date fechaFinEstimada) {
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }
}
