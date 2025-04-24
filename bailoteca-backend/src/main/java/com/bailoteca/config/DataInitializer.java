package com.bailoteca.config;

import com.bailoteca.models.clase.Clase;
import com.bailoteca.models.clase.HorarioClase;
import com.bailoteca.models.enums.Dificultad;
import com.bailoteca.models.enums.Rol;
import com.bailoteca.models.evento.EstadoEvento;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.inscripcion.Inscripcion;
import com.bailoteca.models.pago.PagoEvento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.repository.clase.ClaseRepo;
import com.bailoteca.repository.clase.HorarioClaseRepo;
import com.bailoteca.repository.evento.EventoRepo;
import com.bailoteca.repository.inscripcion.InscripcionRepo;
import com.bailoteca.repository.pago.PagoEventoRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UsuarioRepo usuarioRepo;
    private final ClaseRepo claseRepo;
    private final HorarioClaseRepo horarioClaseRepo;
    private final EventoRepo eventoRepo;
    private final InscripcionRepo inscripcionRepo;
    private final PagoEventoRepo pagoEventoRepo;

    @PostConstruct
    public void initData() {
        if (usuarioRepo.findByCorreo("admin@bailoteca.com").isPresent()) {
            System.out.println("Datos ya insertados anteriormente. Inicialización omitida.");
            return;
        }

        System.out.println("Insertando datos de prueba...");

        // Admin
        Usuario admin = Usuario.builder()
                .nombre("Antonio")
                .apellido("Admin")
                .correo("admin@bailoteca.com")
                .contrasenna("admin123")
                .rol(Rol.ADMIN)
                .telefono("600000000")
                .direccion("Calle Admin, 1")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .activo(true)
                .pagado(true)
                .fechaRegistro(LocalDate.now())
                .build();
        usuarioRepo.save(admin);

        // Profesores
        Usuario tomas = usuarioRepo.save(Usuario.builder()
                .nombre("Tomás")
                .apellido("Profesor")
                .correo("tomas@bailoteca.com")
                .contrasenna("123456")
                .rol(Rol.PROFESOR)
                .telefono("697281258")
                .direccion("Calle Flamenco, 3")
                .activo(true)
                .pagado(true)
                .fechaRegistro(LocalDate.now())
                .build());

        Usuario alba = usuarioRepo.save(Usuario.builder()
                .nombre("Alba")
                .apellido("Profesora")
                .correo("alba@bailoteca.com")
                .contrasenna("123456")
                .rol(Rol.PROFESOR)
                .telefono("659930437")
                .direccion("Calle Salsa, 5")
                .activo(true)
                .pagado(true)
                .fechaRegistro(LocalDate.now())
                .build());

        Usuario javi = usuarioRepo.save(Usuario.builder()
                .nombre("Javi")
                .apellido("Parra")
                .correo("javi@bailoteca.com")
                .contrasenna("123456")
                .rol(Rol.PROFESOR)
                .telefono("692352064")
                .direccion("Calle Bachata, 8")
                .activo(true)
                .pagado(true)
                .fechaRegistro(LocalDate.now())
                .build());

        // Usuario de prueba
        Usuario demoUser = usuarioRepo.save(Usuario.builder()
                .nombre("Pedro")
                .apellido("Martínez")
                .correo("pedro@bailoteca.com")
                .contrasenna("123456")
                .rol(Rol.USUARIO)
                .direccion("Calle Merengue, 9")
                .activo(true)
                .pagado(false)
                .fechaRegistro(LocalDate.now())
                .build());

        // Clases
        Clase latino1 = claseRepo.save(Clase.builder()
                .nombre("Latino Inicial 1")
                .descripcion("Clases con Tomás - lunes y miércoles")
                .profesor(tomas)
                .ubicacion("Edificio Zentro, C. Olivo, 9, 30009 Murcia")
                .dificultad(Dificultad.INICIAL)
                .videoPresentacion("https://www.instagram.com/edificio.zentro/")
                .build());

        Clase latino2 = claseRepo.save(Clase.builder()
                .nombre("Latino Inicial 2")
                .descripcion("Clases con Alba - lunes y miércoles")
                .profesor(alba)
                .ubicacion("La Bailoteca, Calle Floridablanca, 2, 30002 Murcia")
                .dificultad(Dificultad.INICIAL)
                .videoPresentacion("https://instagram.com/edificio.zentro")
                .build());

        Clase latinoAv = claseRepo.save(Clase.builder()
                .nombre("Latino Avanzando")
                .descripcion("Clases avanzadas martes")
                .profesor(tomas)
                .ubicacion("La Bailoteca, Calle Floridablanca, 2, 30002 Murcia")
                .dificultad(Dificultad.AVANZADO)
                .videoPresentacion("https://instagram.com/labailoteca")
                .build());

        Clase latinoInter = claseRepo.save(Clase.builder()
                .nombre("Latino Intermedio")
                .descripcion("Clases jueves")
                .profesor(alba)
                .ubicacion("Edificio Zentro, C. Olivo, 9, 30009 Murcia")
                .dificultad(Dificultad.INTERMEDIO)
                .videoPresentacion("https://instagram.com/labailoteca")
                .build());

        Clase viernesJavi = claseRepo.save(Clase.builder()
                .nombre("Latino Inicial/Intermedio (Javi Parra)")
                .descripcion("Viernes en La Bailoteca")
                .profesor(javi)
                .ubicacion("La Bailoteca, Calle Floridablanca, 2, 30002 Murcia")
                .dificultad(Dificultad.INTERMEDIO)
                .videoPresentacion("https://instagram.com/labailoteca")
                .build());

        // Horarios
        horarioClaseRepo.saveAll(List.of(
                new HorarioClase(null, "LUNES", LocalTime.of(20, 30), LocalTime.of(21, 30), latino1),
                new HorarioClase(null, "LUNES", LocalTime.of(21, 30), LocalTime.of(22, 30), latino2),
                new HorarioClase(null, "MARTES", LocalTime.of(21, 0), LocalTime.of(22, 30), latinoAv),
                new HorarioClase(null, "JUEVES", LocalTime.of(20, 30), LocalTime.of(22, 0), latinoInter),
                new HorarioClase(null, "VIERNES", LocalTime.of(19, 30), LocalTime.of(21, 0), viernesJavi),
                new HorarioClase(null, "VIERNES", LocalTime.of(21, 0), LocalTime.of(22, 0), viernesJavi)
        ));

        // Evento de prueba
        Evento evento = eventoRepo.save(Evento.builder()
                .nombre("Social Bachata")
                .descripcion("Evento social gratuito de prueba")
                .fecha(LocalDateTime.now().plusWeeks(1))
                .lugar("La Bailoteca")
                .estado(EstadoEvento.ACTIVO)
                .organizador(admin)
                .build());

        // Inscripción prueba
        Inscripcion inscripcion = inscripcionRepo.save(Inscripcion.builder()
                .usuario(demoUser)
                .clase(latino1)
                .fechaInscripcion(LocalDate.now())
                .build());

        // Pago de evento prueba
        PagoEvento pagoEvento = PagoEvento.builder()
                .usuario(demoUser)
                .evento(evento)
                .cantidad(5.0)
                .fechaPago(LocalDate.now())
                .build();
        pagoEventoRepo.save(pagoEvento);

        System.out.println("Datos insertados correctamente.");
    }
}