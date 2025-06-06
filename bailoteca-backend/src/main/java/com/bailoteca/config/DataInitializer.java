package com.bailoteca.config;

import com.bailoteca.models.clase.Clase;
import com.bailoteca.models.clase.HorarioClase;
import com.bailoteca.models.enums.Dificultad;
import com.bailoteca.models.enums.EstadoEvento;
import com.bailoteca.models.enums.Rol;
import com.bailoteca.models.evento.Evento;
import com.bailoteca.models.inscripcion.Inscripcion;
import com.bailoteca.models.pago.PagoEvento;
import com.bailoteca.models.usuario.Usuario;
import com.bailoteca.models.evento.AsistenciaEvento;
import com.bailoteca.repository.clase.ClaseRepo;
import com.bailoteca.repository.evento.EventoRepo;
import com.bailoteca.repository.inscripcion.InscripcionRepo;
import com.bailoteca.repository.pago.PagoEventoRepo;
import com.bailoteca.repository.usuario.UsuarioRepo;
import com.bailoteca.repository.evento.AsistenciaEventoRepo;
import com.google.firebase.auth.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.firebase.auth.UserRecord.CreateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Inicializador de datos de prueba para la aplicación.
 * Crea usuarios, clases, eventos e inscripciones de ejemplo.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer {

    @Autowired
    private FirebaseAuth firebaseAuth;

    private final UsuarioRepo usuarioRepo;
    private final ClaseRepo claseRepo;
    private final EventoRepo eventoRepo;
    private final InscripcionRepo inscripcionRepo;
    private final PagoEventoRepo pagoEventoRepo;
    private final AsistenciaEventoRepo asistenciaEventoRepo;

    private void crearUsuarioFirebaseSiNoExiste(String email, String password, String displayName) {
        try {
            firebaseAuth.getUserByEmail(email);
            System.out.println("Usuario ya registrado en Firebase: " + email);
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                try {
                    CreateRequest request = new CreateRequest()
                            .setEmail(email)
                            .setPassword(password)
                            .setDisplayName(displayName);
                    firebaseAuth.createUser(request);
                    System.out.println("Usuario registrado en Firebase: " + email);
                } catch (FirebaseAuthException ex) {
                    System.err.println("Error creando usuario Firebase: " + email);
                    ex.printStackTrace();
                }
            } else {
                System.err.println("Error buscando usuario Firebase: " + email);
                e.printStackTrace();
            }
        }
    }

    @PostConstruct
    public void initData() {
        if (usuarioRepo.findByCorreo("admin@bailoteca.com").isPresent()) {
            System.out.println("Datos ya insertados anteriormente. Inicialización omitida.");
            return;
        }

        System.out.println("Insertando datos de prueba...");

        // Usuarios
        Usuario admin = usuarioRepo.save(Usuario.builder()
                .nombre("Antonio").apellido("Admin").correo("admin@bailoteca.com").contrasenna("admin123")
                .rol(Rol.ADMIN).telefono("600000000").direccion("Calle Admin, 1")
                .fechaNacimiento(LocalDate.of(1990, 1, 1)).activo(true).pagado(true)
                .fechaRegistro(LocalDate.now()).build());
        crearUsuarioFirebaseSiNoExiste(admin.getCorreo(), admin.getContrasenna(), admin.getNombre() + " " + admin.getApellido());

        Usuario tomas = usuarioRepo.save(Usuario.builder()
                .nombre("Tomás").apellido("Profesor").correo("tomas@bailoteca.com").contrasenna("123456")
                .rol(Rol.PROFESOR).telefono("697281258").direccion("Calle Flamenco, 3")
                .activo(true).pagado(true).fechaRegistro(LocalDate.now()).build());
        crearUsuarioFirebaseSiNoExiste(tomas.getCorreo(), tomas.getContrasenna(), tomas.getNombre() + " " + tomas.getApellido());

        Usuario alba = usuarioRepo.save(Usuario.builder()
                .nombre("Alba").apellido("Profesora").correo("alba@bailoteca.com").contrasenna("123456")
                .rol(Rol.PROFESOR).telefono("659930437").direccion("Calle Salsa, 5")
                .activo(true).pagado(true).fechaRegistro(LocalDate.now()).build());
        crearUsuarioFirebaseSiNoExiste(alba.getCorreo(), alba.getContrasenna(), alba.getNombre() + " " + alba.getApellido());

        Usuario javi = usuarioRepo.save(Usuario.builder()
                .nombre("Javi").apellido("Parra").correo("javi@bailoteca.com").contrasenna("123456")
                .rol(Rol.PROFESOR).telefono("692352064").direccion("Calle Bachata, 8")
                .activo(true).pagado(true).fechaRegistro(LocalDate.now()).build());
        crearUsuarioFirebaseSiNoExiste(javi.getCorreo(), javi.getContrasenna(), javi.getNombre() + " " + javi.getApellido());

        Usuario demoUser = usuarioRepo.save(Usuario.builder()
                .nombre("Pedro").apellido("Martínez").correo("pedro@bailoteca.com").contrasenna("123456")
                .rol(Rol.USUARIO).direccion("Calle Merengue, 9")
                .activo(true).pagado(false).fechaRegistro(LocalDate.now()).build());
        crearUsuarioFirebaseSiNoExiste(demoUser.getCorreo(), demoUser.getContrasenna(), demoUser.getNombre() + " " + demoUser.getApellido());

        // Clases
        Clase latino1 = Clase.builder().nombre("Latino Inicial 1").descripcion("Clases con Tomás - lunes y miércoles")
                .profesor(tomas).ubicacion("Edificio Zentro, C. Olivo, 9, 30009 Murcia")
                .dificultad(Dificultad.INICIAL).videoPresentacion("https://www.instagram.com/edificio.zentro/")
                .publica(true).build();

        Clase latino2 = Clase.builder().nombre("Latino Inicial 2").descripcion("Clases con Alba - lunes y miércoles")
                .profesor(alba).ubicacion("La Bailoteca, Calle Floridablanca, 2, 30002 Murcia")
                .dificultad(Dificultad.INICIAL).videoPresentacion("https://instagram.com/edificio.zentro")
                .publica(true).build();

        Clase latinoAv = Clase.builder().nombre("Latino Avanzado").descripcion("Clases avanzadas martes")
                .profesor(tomas).ubicacion("La Bailoteca, Calle Floridablanca, 2, 30002 Murcia")
                .dificultad(Dificultad.AVANZADO).videoPresentacion("https://instagram.com/labailoteca")
                .publica(true).build();

        Clase latinoInter = Clase.builder().nombre("Latino Intermedio").descripcion("Clases jueves")
                .profesor(alba).ubicacion("Edificio Zentro, C. Olivo, 9, 30009 Murcia")
                .dificultad(Dificultad.INTERMEDIO).videoPresentacion("https://instagram.com/labailoteca")
                .publica(true).build();

        Clase viernesJavi = Clase.builder().nombre("Latino Inicial/Intermedio (Javi Parra)")
                .descripcion("Viernes en La Bailoteca").profesor(javi)
                .ubicacion("La Bailoteca, Calle Floridablanca, 2, 30002 Murcia")
                .dificultad(Dificultad.INTERMEDIO).videoPresentacion("https://instagram.com/labailoteca")
                .publica(true).build();

        HorarioClase h1 = new HorarioClase(null, "LUNES", LocalTime.of(20, 30), LocalTime.of(21, 30), latino1);
        HorarioClase h2 = new HorarioClase(null, "LUNES", LocalTime.of(21, 30), LocalTime.of(22, 30), latino2);
        HorarioClase h3 = new HorarioClase(null, "MARTES", LocalTime.of(21, 0), LocalTime.of(22, 30), latinoAv);
        HorarioClase h4 = new HorarioClase(null, "JUEVES", LocalTime.of(20, 30), LocalTime.of(22, 0), latinoInter);
        HorarioClase h5 = new HorarioClase(null, "VIERNES", LocalTime.of(19, 30), LocalTime.of(21, 0), viernesJavi);
        HorarioClase h6 = new HorarioClase(null, "VIERNES", LocalTime.of(21, 0), LocalTime.of(22, 0), viernesJavi);

        latino1.setHorarioClases(List.of(h1));
        latino2.setHorarioClases(List.of(h2));
        latinoAv.setHorarioClases(List.of(h3));
        latinoInter.setHorarioClases(List.of(h4));
        viernesJavi.setHorarioClases(List.of(h5, h6));

        claseRepo.saveAll(List.of(latino1, latino2, latinoAv, latinoInter, viernesJavi));

        // Evento de prueba
        Evento evento1 = eventoRepo.save(Evento.builder()
                .nombre("Fiesta de Bienvenida").descripcion("Evento social abierto al público")
                .fecha(LocalDateTime.now().plusDays(5)).lugar("Sala 1 - La Bailoteca")
                .estado(EstadoEvento.ACTIVO).organizador(tomas).publico(true).build());

        Evento evento2 = eventoRepo.save(Evento.builder()
                .nombre("Taller de Salsa Avanzado").descripcion("Solo para alumnos inscritos")
                .fecha(LocalDateTime.now().plusDays(10)).lugar("Edificio Zentro")
                .estado(EstadoEvento.ACTIVO).organizador(alba).publico(false).build());

        // Registro de asistencia ficticia
        asistenciaEventoRepo.save(new AsistenciaEvento(null, evento1, demoUser, true, true));
        asistenciaEventoRepo.save(new AsistenciaEvento(null, evento2, demoUser, false, false));

        // Inscripción y pago ficticio
        inscripcionRepo.save(Inscripcion.builder()
                .usuario(demoUser).clase(latino1).fechaInscripcion(LocalDate.now()).build());

        pagoEventoRepo.save(PagoEvento.builder()
                .usuario(demoUser).evento(evento1).cantidad(5.0).fechaPago(LocalDate.now()).build());

        System.out.println("Datos insertados correctamente.");
    }
}