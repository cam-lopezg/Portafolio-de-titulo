package cl.pets.patitaspet.appointment.repository;

import cl.pets.patitaspet.appointment.entity.ImportantDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// RECORDAR QUE SE COMENTO; NO SE SE SI LO USARE extends JpaRepository<ImportantDate, Long>
public interface ImportantDateRepository {


     public ImportantDate save(ImportantDate importantDate);

     public List<ImportantDate> findAll();
 }






