package com.quiniela.pojo;

import java.time.LocalDateTime;

public class PartidoDTO {
    private Partido partido;
    private Prediccion prediccion; // Será null si el usuario no ha apostado aún
    
    public PartidoDTO(Partido partido, Prediccion prediccion) {
        this.partido = partido;
        this.prediccion = prediccion;
    }
    
    public Partido getPartido() { return partido; }
    public Prediccion getPrediccion() { return prediccion; }
    
    public LocalDateTime getLimiteApuesta() {
        if (partido.getFechaPartido() == null) return null;
        return partido.getFechaPartido().minusHours(1);
    }
    
}
