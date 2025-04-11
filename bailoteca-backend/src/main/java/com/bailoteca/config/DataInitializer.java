package com.bailoteca.config;

import com.bailoteca.models.clase.Clase;
import com.bailoteca.models.clase.HorarioClase;
import com.bailoteca.models.enums.Rol;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.clase.ClaseRepo;
import com.bailoteca.repository.clase.HorarioClaseRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Inicializador de datos que se ejecuta al arrancar la aplicación.
 * Crea usuarios (admin, profesores, demo), clases y horarios predefinidos.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UsuarioRepo usuarioRepo;
    private final ClaseRepo claseRepo;
    private final HorarioClaseRepo horarioClaseRepo;

    @PostConstruct
    public void initData() {
        // Evita insertar duplicados si los datos ya existen
        if (usuarioRepo.count() > 0) {
            System.out.println("Los datos ya existen. Inicialización omitida.");
            return;
        }

        System.out.println("Insertando datos de prueba...");
        // Crear Admin
        Usuario admin = Usuario.builder()
                .nombre("Antonio")
                .apellido("ApellidoAdmin")
                .correo("admin@bailoteca.com")
                .contrasenna("admin123")
                .rol(Rol.ADMIN)
                .telefono("600000000")
                .direccion("Calle Admin")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .activo(true)
                .pagado(true)
                .fechaRegistro(LocalDate.now())
                .build();
        usuarioRepo.save(admin);

        // Crear Profesores
        Usuario tomas = Usuario.builder()
                .nombre("Tomás")
                .apellido("Profesor")
                .correo("tomas@bailoteca.com")
                .contrasenna("123456")
                .rol(Rol.PROFESOR)
                .telefono("697281258")
                .activo(true)
                .pagado(true)
                .fechaRegistro(LocalDate.now())
                .build();

        Usuario alba = Usuario.builder()
                .nombre("Alba")
                .apellido("Profesora")
                .correo("alba@bailoteca.com")
                .contrasenna("123456")
                .rol(Rol.PROFESOR)
                .telefono("659930437")
                .activo(true)
                .pagado(true)
                .fechaRegistro(LocalDate.now())
                .build();

        Usuario javi = Usuario.builder()
                .nombre("Javi")
                .apellido("Parra")
                .correo("javi@bailoteca.com")
                .contrasenna("123456")
                .rol(Rol.PROFESOR)
                .telefono("692352064")
                .activo(true)
                .pagado(true)
                .fechaRegistro(LocalDate.now())
                .build();

        usuarioRepo.saveAll(List.of(tomas, alba, javi));

        // Crear clases
        Clase latino1 = Clase.builder()
                .nombre("Latino Inicial 1")
                .descripcion("Clases con Tomás y Alba - lunes y miércoles")
                .profesor(tomas)
                .videoPresentacion("https://www.instagram.com/edificio.zentro/")
                .build();

        Clase latino2 = Clase.builder()
                .nombre("Latino Inicial 2")
                .descripcion("Clases con Tomás y Alba - lunes y miércoles")
                .profesor(alba)
                .videoPresentacion("https://instagram.com/edificio.zentro")
                .build();

        Clase latinoAv = Clase.builder()
                .nombre("Latino Avanzando")
                .descripcion("Clases martes (proximamente)")
                .profesor(tomas)
                .videoPresentacion("https://instagram.com/labailoteca")
                .build();

        Clase latinoInter = Clase.builder()
                .nombre("Latino Intermedio")
                .descripcion("Clases jueves")
                .profesor(alba)
                .videoPresentacion("https://instagram.com/labailoteca")
                .build();

        Clase viernesJavi = Clase.builder()
                .nombre("Latino Inicial/Intermedio (Javi Parra)")
                .descripcion("Viernes en La Bailoteca")
                .profesor(javi)
                .videoPresentacion("https://instagram.com/labailoteca")
                .build();

        claseRepo.saveAll(List.of(latino1, latino2, latinoAv, latinoInter, viernesJavi));

        // Crear horarios
        horarioClaseRepo.saveAll(List.of(
                new HorarioClase(null, "LUNES", LocalTime.of(20, 30), LocalTime.of(21, 30), latino1),
                new HorarioClase(null, "LUNES", LocalTime.of(21, 30), LocalTime.of(22, 30), latino2),
                new HorarioClase(null, "MARTES", LocalTime.of(21, 0), LocalTime.of(22, 30), latinoAv),
                new HorarioClase(null, "JUEVES", LocalTime.of(20, 30), LocalTime.of(22, 0), latinoInter),
                new HorarioClase(null, "VIERNES", LocalTime.of(19, 30), LocalTime.of(21, 0), viernesJavi),
                new HorarioClase(null, "VIERNES", LocalTime.of(21, 0), LocalTime.of(22, 0), viernesJavi)
        ));

        // Crear usuario de prueba
        Usuario demoUser = Usuario.builder()
                .nombre("Pedro")
                .apellido("Martínez")
                .correo("pedro@bailoteca.com")
                .contrasenna("123456")
                .rol(Rol.USUARIO)
                .activo(true)
                .pagado(false)
                .fechaRegistro(LocalDate.now())
                .build();
        usuarioRepo.save(demoUser);

        System.out.println("Datos insertados correctamente!");
    }
}
