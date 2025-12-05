package com.example.serviciospublicos.models;

import com.example.serviciospublicos.models.enums.RolUsuario;
import com.example.serviciospublicos.models.enums.TipoEvidencia;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Evidencia {

    private String id;
    private String obraId;

    private String usuarioId;
    private String usuarioNombre;
    private RolUsuario rolUsuario;

    private TipoEvidencia tipo;   // FOTO o VIDEO
    private String urlArchivo;
    private String descripcion;
    private String etapaConstruccion;

    private double lat;
    private double lng;

    @ServerTimestamp
    private Date fechaHora;

    // estado: "pendiente", "aprobada", "rechazada"
    private String estado;
    private String aprobadoPor;   // uid supervisor
    private Date fechaRevision;

    public Evidencia() { }

    public Evidencia(String id,
                     String obraId,
                     String usuarioId,
                     String usuarioNombre,
                     RolUsuario rolUsuario,
                     TipoEvidencia tipo,
                     String urlArchivo,
                     String descripcion,
                     String etapaConstruccion,
                     double lat,
                     double lng,
                     String estado,
                     String aprobadoPor,
                     Date fechaRevision) {

        this.id = id;
        this.obraId = obraId;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.rolUsuario = rolUsuario;
        this.tipo = tipo;
        this.urlArchivo = urlArchivo;
        this.descripcion = descripcion;
        this.etapaConstruccion = etapaConstruccion;
        this.lat = lat;
        this.lng = lng;
        this.estado = estado;
        this.aprobadoPor = aprobadoPor;
        this.fechaRevision = fechaRevision;
    }

    // ----- Getters y Setters -----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObraId() {
        return obraId;
    }

    public void setObraId(String obraId) {
        this.obraId = obraId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public RolUsuario getRolUsuario() {
        return rolUsuario;
    }

    public void setRolUsuario(RolUsuario rolUsuario) {
        this.rolUsuario = rolUsuario;
    }

    public TipoEvidencia getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvidencia tipo) {
        this.tipo = tipo;
    }

    public String getUrlArchivo() {
        return urlArchivo;
    }

    public void setUrlArchivo(String urlArchivo) {
        this.urlArchivo = urlArchivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEtapaConstruccion() {
        return etapaConstruccion;
    }

    public void setEtapaConstruccion(String etapaConstruccion) {
        this.etapaConstruccion = etapaConstruccion;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public Date getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getAprobadoPor() {
        return aprobadoPor;
    }

    public void setAprobadoPor(String aprobadoPor) {
        this.aprobadoPor = aprobadoPor;
    }

    public Date getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(Date fechaRevision) {
        this.fechaRevision = fechaRevision;
    }
}
