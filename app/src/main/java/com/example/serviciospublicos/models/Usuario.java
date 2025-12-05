package com.example.serviciospublicos.models;

import com.example.serviciospublicos.models.enums.RolUsuario;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Usuario {

    private String id;                 // uid de FirebaseAuth
    private String nombre;
    private String correo;
    private RolUsuario rol;
    private List<String> obrasAsignadas;

    @ServerTimestamp
    private Date fechaRegistro;

    // Constructor vacío requerido por Firestore
    public Usuario() { }

    public Usuario(String id,
                   String nombre,
                   String correo,
                   RolUsuario rol,
                   List<String> obrasAsignadas) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.obrasAsignadas = obrasAsignadas;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public List<String> getObrasAsignadas() {
        return obrasAsignadas;
    }

    public void setObrasAsignadas(List<String> obrasAsignadas) {
        this.obrasAsignadas = obrasAsignadas;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
