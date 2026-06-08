package com.quiniela.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.quiniela.dao.PosicionesMundialDao;
import com.quiniela.pojo.EstadisticaEquipo;

@Repository
@Transactional
public class PosicionesMundialDaoImpl implements PosicionesMundialDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void guardarOActualizar(EstadisticaEquipo estadistica) {
        sessionFactory.getCurrentSession().saveOrUpdate(estadistica);
    }

    @Override
    public void vaciarTabla() {
        sessionFactory.getCurrentSession()
                .createQuery("DELETE FROM EstadisticaEquipo")
                .executeUpdate();
    }

    @Override
    public List<EstadisticaEquipo> obtenerTablaOrdenada() {
        String hql = "FROM EstadisticaEquipo ORDER BY puntos DESC, diferenciaGoles DESC, golesFavor DESC";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, EstadisticaEquipo.class)
                .getResultList();
    }
}
