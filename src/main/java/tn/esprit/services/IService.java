package tn.esprit.services;

import java.util.List;

public interface IService<T> {
    void create(T t);
    List<T> getAll();
    void update(T t);
    void delete(int id);
}