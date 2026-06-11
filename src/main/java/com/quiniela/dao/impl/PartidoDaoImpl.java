package com.quiniela.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.quiniela.dao.PartidoDao;
import com.quiniela.pojo.Partido;

@Repository
@Transactional
public class PartidoDaoImpl implements PartidoDao {

    private static final Logger log = LoggerFactory.getLogger(PartidoDaoImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void guardar(Partido partido) {
        log.debug("Guardando partido: {} vs {}", partido.getEquipoLocal(), partido.getEquipoVisitante());
        sessionFactory.getCurrentSession().save(partido);
    }

    @Override
    public List<Partido> obtenerTodos() {
        return sessionFactory.getCurrentSession()
                .createQuery(
                    "SELECT DISTINCT p FROM Partido p LEFT JOIN FETCH p.fase ORDER BY p.fechaPartido ASC",
                    Partido.class)
                .getResultList();
    }

    @Override
    public Partido buscarPorId(Long id) {
        return sessionFactory.getCurrentSession().get(Partido.class, id);
    }

    @Override
    public void actualizar(Partido partido) {
        sessionFactory.getCurrentSession().update(partido);
    }

    @Override
    public void eliminar(Long id) {
        Partido partido = buscarPorId(id);
        if (partido != null) {
            sessionFactory.getCurrentSession().delete(partido);
        }
    }

    @Override
    public List<Partido> obtenerPorFase(Long faseId) {
        String hql = "FROM Partido p " +
                     "LEFT JOIN FETCH p.fase " +
                     "WHERE p.fase.id = :faseId " +                     
                     "ORDER BY p.fechaPartido";

        return sessionFactory.getCurrentSession()
                .createQuery(hql, Partido.class)
                .setParameter("faseId", faseId)
                .list();
    }
    
    @Override
    public List<Partido> obtenerPartidosPorFasesActivas(List<Long>  faseId) {
        String hql = "SELECT DISTINCT p FROM Partido p " +
                "LEFT JOIN FETCH p.fase " +
                "WHERE p.fase.id IN (:ids) " +
                "ORDER BY p.fechaPartido";

        return sessionFactory.getCurrentSession()
           .createQuery(hql, Partido.class)
           .setParameter("ids", faseId)
           .list();
    
    }
}
