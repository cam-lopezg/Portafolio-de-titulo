package cl.pets.patitaspet.appointment.dto;


import cl.pets.patitaspet.pet.entity.Species;

    public class ImportantDateCreateRequest {

        //String name, String date (MM-DD), String description, Species species

        private String name;
        private String date;
        private String description;
        private Species species;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Species getSpecies() {
            return species;
        }

        public void setSpecies(Species species) {
            this.species = species;
        }
    }




