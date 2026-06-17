package com.quiniela.dao.impl;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.quiniela.dao.PrediccionDao;
import com.quiniela.pojo.Prediccion;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
@Transactional
public class PrediccionDaoImpl implements PrediccionDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void guardar(Prediccion prediccion) {    	
    	
    	if ( prediccion.getFechaRegistro() != null ) {    		
    		prediccion.setFechaModificacion(LocalDateTime.now(ZoneId.of("America/Mexico_City")));    		
    	} else {    		
    		prediccion.setFechaRegistro(LocalDateTime.now(ZoneId.of("America/Mexico_City")));    		
    	}    	
    	
        sessionFactory.getCurrentSession().saveOrUpdate(prediccion);
        sessionFactory.getCurrentSession().flush(); 
    }

    @Override
    public List<Prediccion> buscarPorUsuario(Long usuarioId) {
        String hql = "FROM Prediccion WHERE usuario.id = :usuarioId";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Prediccion.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }

    @Override
    public Prediccion buscarPorUsuarioYPartido(Long usuarioId, Long partidoId) {
        String hql = "FROM Prediccion WHERE usuario.id = :usuarioId AND partido.id = :partidoId";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Prediccion.class)
                .setParameter("usuarioId", usuarioId)
                .setParameter("partidoId", partidoId)
                .uniqueResult();
    }

    @Override
    public List<Prediccion> obtenerTodas() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Prediccion", Prediccion.class)
                .getResultList();
    }
   
    
    @Override
    public void actualizar(Prediccion prediccion) {
        sessionFactory.getCurrentSession().update(prediccion);
    }
    
    @Override
    public int sumarPuntosPorUsuario(Long usuarioId) {
        String hql = "SELECT SUM(p.puntosGanados) FROM Prediccion p " +
                      "WHERE p.usuario.id = :usuarioId AND p.puntosGanados IS NOT NULL";

        Long suma = (Long) sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("usuarioId", usuarioId)
                .uniqueResult();

        return (suma != null) ? suma.intValue() : 0;
    }
    
    @Override
    public List<Prediccion> obtenerTodasConPartido() {
        String hql = "SELECT DISTINCT p FROM Prediccion p " +
                     "JOIN FETCH p.partido partido " +
                     "JOIN FETCH partido.fase " +
                     "JOIN FETCH p.usuario";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Prediccion.class)
                .getResultList();
    }
}
