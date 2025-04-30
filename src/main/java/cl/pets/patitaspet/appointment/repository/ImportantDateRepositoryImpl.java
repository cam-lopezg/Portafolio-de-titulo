package cl.pets.patitaspet.appointment.repository;


import cl.pets.patitaspet.appointment.entity.ImportantDate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ImportantDateRepositoryImpl implements ImportantDateRepository {
    @Override
    public ImportantDate save(ImportantDate importantDate) {

        //escribir codigo ´para guardar aqui

        return null;
    }

    @Override
    public List<ImportantDate> findAll() {

        //escribir codigo para buscar aqui


        return List.of();
    }
}
