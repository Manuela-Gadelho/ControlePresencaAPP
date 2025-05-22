package com.example.leitorqrcode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AttendanceDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AttendanceDB.db";
    // Aumente a versão do DB para forçar o onUpgrade.
    // Se você já estava na 3, mude para 4. Isso garante que o onCreate seja chamado para recriar as tabelas.
    private static final int DATABASE_VERSION = 4;

    // Seus nomes de tabelas
    private static final String TABLE_ORGANIZERS = "Organizers";
    private static final String TABLE_PARTICIPANTS = "Participants";
    private static final String TABLE_EVENTS = "Events";
    private static final String TABLE_CHECKINS = "CheckIns"; // Sua tabela de check-ins

    // Suas constantes de colunas para ORGANIZERS
    private static final String COLUMN_ORGANIZER_ID = "OrganizerId";
    private static final String COLUMN_ORGANIZER_USERNAME = "Username";
    private static final String COLUMN_ORGANIZER_PASSWORD = "Password";
    private static final String COLUMN_ORGANIZER_EMAIL = "Email";

    // Suas constantes de colunas para PARTICIPANTS
    private static final String COLUMN_PARTICIPANT_ID = "ParticipantId";
    private static final String COLUMN_PARTICIPANT_NAME = "Name";
    private static final String COLUMN_PARTICIPANT_EMAIL = "Email";
    private static final String COLUMN_PARTICIPANT_QRCODE_ID = "QrCodeId"; // Sua constante
    private static final String COLUMN_PARTICIPANT_EVENT_ID_FK = "EventId"; // Sua constante

    // Suas constantes de colunas para EVENTS
    private static final String COLUMN_EVENT_ID = "EventId";
    private static final String COLUMN_EVENT_NAME = "EventName";
    private static final String COLUMN_EVENT_DESCRIPTION = "EventDescription";
    private static final String COLUMN_EVENT_DATE = "EventDate";
    private static final String COLUMN_EVENT_ORGANIZER_ID_FK = "OrganizerId"; // Sua constante

    // Suas constantes de colunas para CHECKINS
    private static final String COLUMN_CHECKIN_ID = "CheckInId";
    private static final String COLUMN_CHECKIN_PARTICIPANT_ID_FK = "ParticipantId"; // Sua constante
    private static final String COLUMN_CHECKIN_EVENT_ID_FK = "EventId"; // Sua constante
    private static final String COLUMN_CHECKIN_TIMESTAMP = "Timestamp";
    private static final String COLUMN_CHECKIN_TYPE = "Type"; // NOVA COLUNA PARA ENTRADA/SAÍDA

    private static final String TAG = "AttendanceDbHelper";

    public AttendanceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ORGANIZERS_TABLE = "CREATE TABLE " + TABLE_ORGANIZERS + "("
                + COLUMN_ORGANIZER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORGANIZER_USERNAME + " TEXT NOT NULL UNIQUE,"
                + COLUMN_ORGANIZER_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_ORGANIZER_EMAIL + " TEXT NOT NULL" + ")";
        db.execSQL(CREATE_ORGANIZERS_TABLE);
        Log.d(TAG, "Tabela Organizers criada: " + CREATE_ORGANIZERS_TABLE);

        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EVENT_NAME + " TEXT NOT NULL,"
                + COLUMN_EVENT_DESCRIPTION + " TEXT,"
                + COLUMN_EVENT_DATE + " TEXT NOT NULL,"
                + COLUMN_EVENT_ORGANIZER_ID_FK + " INTEGER NOT NULL,"
                + " FOREIGN KEY (" + COLUMN_EVENT_ORGANIZER_ID_FK + ") REFERENCES " + TABLE_ORGANIZERS + "(" + COLUMN_ORGANIZER_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_EVENTS_TABLE);
        Log.d(TAG, "Tabela Events criada: " + CREATE_EVENTS_TABLE);

        String CREATE_PARTICIPANTS_TABLE = "CREATE TABLE " + TABLE_PARTICIPANTS + "("
                + COLUMN_PARTICIPANT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PARTICIPANT_NAME + " TEXT NOT NULL,"
                + COLUMN_PARTICIPANT_EMAIL + " TEXT NOT NULL,"
                + COLUMN_PARTICIPANT_QRCODE_ID + " TEXT NOT NULL UNIQUE," // Usando sua constante
                + COLUMN_PARTICIPANT_EVENT_ID_FK + " INTEGER NOT NULL," // Usando sua constante
                + " FOREIGN KEY (" + COLUMN_PARTICIPANT_EVENT_ID_FK + ") REFERENCES " + TABLE_EVENTS + "(" + COLUMN_EVENT_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_PARTICIPANTS_TABLE);
        Log.d(TAG, "Tabela Participants criada: " + CREATE_PARTICIPANTS_TABLE);

        // MODIFICAÇÃO AQUI: Adicionando a nova coluna COLUMN_CHECKIN_TYPE
        String CREATE_CHECKINS_TABLE = "CREATE TABLE " + TABLE_CHECKINS + "("
                + COLUMN_CHECKIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CHECKIN_PARTICIPANT_ID_FK + " INTEGER NOT NULL," // Usando sua constante
                + COLUMN_CHECKIN_EVENT_ID_FK + " INTEGER NOT NULL," // Usando sua constante
                + COLUMN_CHECKIN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP," // sqlite guarda como TEXT em ISO8601 string
                + COLUMN_CHECKIN_TYPE + " TEXT NOT NULL," // NOVA COLUNA ADICIONADA AQUI
                + " FOREIGN KEY (" + COLUMN_CHECKIN_PARTICIPANT_ID_FK + ") REFERENCES " + TABLE_PARTICIPANTS + "(" + COLUMN_PARTICIPANT_ID + ") ON DELETE CASCADE,"
                + " FOREIGN KEY (" + COLUMN_CHECKIN_EVENT_ID_FK + ") REFERENCES " + TABLE_EVENTS + "(" + COLUMN_EVENT_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_CHECKINS_TABLE);
        Log.d(TAG, "Tabela CheckIns criada: " + CREATE_CHECKINS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Atualizando o banco de dados da versão " + oldVersion + " para " + newVersion + ". Destruindo dados antigos.");
        // ATENÇÃO: Ao dropar tabelas, todos os dados são perdidos!
        // Em um app de produção, você precisaria de uma estratégia de migração de dados.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKINS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTICIPANTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORGANIZERS);
        onCreate(db); // Recria todas as tabelas com as novas definições
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    // --- Métodos para Organizadores ---
    public long addOrganizer(String username, String password, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORGANIZER_USERNAME, username);
        values.put(COLUMN_ORGANIZER_PASSWORD, password);
        values.put(COLUMN_ORGANIZER_EMAIL, email);
        long newRowId = -1;

        try {
            newRowId = db.insertOrThrow(TABLE_ORGANIZERS, null, values);
            Log.d(TAG, "addOrganizer: Organizador adicionado, ID: " + newRowId);
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            Log.e(TAG, "addOrganizer: Erro de restrição única (Username já existe) para: " + username + " - " + e.getMessage());
            newRowId = -1;
        } catch (Exception e) {
            Log.e(TAG, "addOrganizer: Erro ao adicionar organizador: " + username + " - " + e.getMessage());
            newRowId = -1;
        } finally {
            db.close();
        }
        return newRowId;
    }

    public long insertOrganizerWithEvent(String username, String password, String email, String eventName, String eventDescription, String eventDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        long organizerId = -1;
        long eventId = -1;
        db.beginTransaction();
        try {
            ContentValues organizerValues = new ContentValues();
            organizerValues.put(COLUMN_ORGANIZER_USERNAME, username);
            organizerValues.put(COLUMN_ORGANIZER_PASSWORD, password);
            organizerValues.put(COLUMN_ORGANIZER_EMAIL, email);
            organizerId = db.insertOrThrow(TABLE_ORGANIZERS, null, organizerValues);
            Log.d(TAG, "insertOrganizerWithEvent: Organizador inserido, ID: " + organizerId);

            if (organizerId != -1) {
                ContentValues eventValues = new ContentValues();
                eventValues.put(COLUMN_EVENT_NAME, eventName);
                eventValues.put(COLUMN_EVENT_DESCRIPTION, eventDescription);
                eventValues.put(COLUMN_EVENT_DATE, eventDate);
                eventValues.put(COLUMN_EVENT_ORGANIZER_ID_FK, organizerId); // Usando a constante correta
                eventId = db.insertOrThrow(TABLE_EVENTS, null, eventValues);
                Log.d(TAG, "insertOrganizerWithEvent: Evento inserido, ID: " + eventId + ", Organizador ID: " + organizerId);

                if (eventId != -1) {
                    db.setTransactionSuccessful();
                } else {
                    Log.e(TAG, "insertOrganizerWithEvent: Falha ao inserir evento. Rollback do organizador. Organizador ID: " + organizerId);
                    organizerId = -1;
                }
            } else {
                Log.e(TAG, "insertOrganizerWithEvent: Falha ao inserir organizador.");
            }
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            Log.e(TAG, "insertOrganizerWithEvent: Erro de restrição única (Username já existe) para: " + username + " - " + e.getMessage());
            organizerId = -1;
            eventId = -1;
        } catch (Exception e) {
            Log.e(TAG, "insertOrganizerWithEvent: Erro geral ao inserir organizador com evento: " + e.getMessage(), e);
            organizerId = -1;
            eventId = -1;
        } finally {
            db.endTransaction();
            db.close();
        }
        return eventId;
    }

    public Organizer getOrganizer(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Organizer organizer = null;

        try {
            Log.d(TAG, "getOrganizer: Buscando organizador com username: '" + username + "'");
            cursor = db.query(
                    TABLE_ORGANIZERS,
                    new String[]{COLUMN_ORGANIZER_ID, COLUMN_ORGANIZER_USERNAME, COLUMN_ORGANIZER_PASSWORD, COLUMN_ORGANIZER_EMAIL},
                    COLUMN_ORGANIZER_USERNAME + "=?",
                    new String[]{username},
                    null, null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                organizer = new Organizer();
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER_ID));
                String dbUsername = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER_USERNAME));
                String dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER_PASSWORD));
                String dbEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER_EMAIL));

                organizer.setId(id);
                organizer.setUsername(dbUsername);
                organizer.setPassword(dbPassword);
                organizer.setEmail(dbEmail);

                Log.d(TAG, "getOrganizer: Organizador ENCONTRADO. ID: " + id + ", Username DB: '" + dbUsername + "'");
            } else {
                Log.d(TAG, "getOrganizer: Organizador NÃO ENCONTRADO com username: '" + username + "'");
            }
        } catch (Exception e) {
            Log.e(TAG, "getOrganizer: Erro ao buscar organizador: " + e.getMessage(), e);
            organizer = null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return organizer;
    }

    public boolean checkOrganizerExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            Log.d(TAG, "checkOrganizerExists: Verificando existência de username: '" + username + "'");
            cursor = db.query(TABLE_ORGANIZERS,
                    new String[]{COLUMN_ORGANIZER_USERNAME},
                    COLUMN_ORGANIZER_USERNAME + "=?",
                    new String[]{username},
                    null, null, null);
            exists = cursor.getCount() > 0;
            Log.d(TAG, "checkOrganizerExists: Organizador '" + username + "' existe: " + exists);
        } catch (Exception e) {
            Log.e(TAG, "checkOrganizerExists: Erro ao verificar existência do organizador: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return exists;
    }

    public List<Organizer> getAllOrganizers() {
        List<Organizer> organizerList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ORGANIZERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    Organizer organizer = new Organizer();
                    organizer.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER_ID)));
                    organizer.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER_USERNAME)));
                    organizer.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER_PASSWORD)));
                    organizer.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER_EMAIL)));
                    organizerList.add(organizer);
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "getAllOrganizers: " + organizerList.size() + " organizadores encontrados.");
        } catch (Exception e) {
            Log.e(TAG, "getAllOrganizers: Erro ao buscar todos os organizadores: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return organizerList;
    }

    // --- Métodos de CRUD para Participantes ---
    public long addParticipant(String name, String email, long eventId, String qrCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARTICIPANT_NAME, name);
        values.put(COLUMN_PARTICIPANT_EMAIL, email);
        values.put(COLUMN_PARTICIPANT_QRCODE_ID, qrCode);
        values.put(COLUMN_PARTICIPANT_EVENT_ID_FK, eventId);

        long newParticipantId = -1;
        try {
            newParticipantId = db.insertOrThrow(TABLE_PARTICIPANTS, null, values);
            Log.d(TAG, "addParticipant: Participante '" + name + "' (ID: " + newParticipantId + ", Event ID: " + eventId + ") adicionado.");
        } catch (SQLiteConstraintException e) {
            Log.e(TAG, "addParticipant: Erro de restrição única (QR Code duplicado) para: " + qrCode + " - " + e.getMessage());
            newParticipantId = -1;
        } catch (Exception e) {
            Log.e(TAG, "addParticipant: Erro ao adicionar participante: " + e.getMessage(), e);
            newParticipantId = -1;
        } finally {
            db.close();
        }
        return newParticipantId;
    }

    // Método addParticipant(Participant participant) - usado se você criar um objeto Participante primeiro
    public long addParticipant(Participant participant) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARTICIPANT_NAME, participant.getName());
        values.put(COLUMN_PARTICIPANT_EMAIL, participant.getEmail());
        values.put(COLUMN_PARTICIPANT_QRCODE_ID, participant.getQrCodeId());
        values.put(COLUMN_PARTICIPANT_EVENT_ID_FK, participant.getEventId());

        long newParticipantId = -1;
        try {
            newParticipantId = db.insertOrThrow(TABLE_PARTICIPANTS, null, values);
            Log.d(TAG, "addParticipant (Object): Participante '" + participant.getName() + "' (ID: " + newParticipantId + ", Event ID: " + participant.getEventId() + ") adicionado.");
        } catch (SQLiteConstraintException e) {
            Log.e(TAG, "addParticipant (Object): Erro de restrição única (QR Code duplicado) para: " + participant.getQrCodeId() + " - " + e.getMessage());
            newParticipantId = -1;
        } catch (Exception e) {
            Log.e(TAG, "addParticipant (Object): Erro ao adicionar participante: " + e.getMessage(), e);
            newParticipantId = -1;
        } finally {
            db.close();
        }
        return newParticipantId;
    }

    public Participant getParticipantById(long participantId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Participant participant = null;
        try {
            cursor = db.query(TABLE_PARTICIPANTS,
                    new String[]{COLUMN_PARTICIPANT_ID, COLUMN_PARTICIPANT_NAME, COLUMN_PARTICIPANT_EMAIL, COLUMN_PARTICIPANT_EVENT_ID_FK, COLUMN_PARTICIPANT_QRCODE_ID},
                    COLUMN_PARTICIPANT_ID + "=?", new String[]{String.valueOf(participantId)}, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                participant = new Participant();
                // Usando getColumnIndexOrThrow para segurança
                participant.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_ID)));
                participant.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_NAME)));
                participant.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_EMAIL)));
                participant.setEventId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_EVENT_ID_FK)));
                participant.setQrCodeId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_QRCODE_ID)));
                Log.d(TAG, "getParticipantById: Participante encontrado, ID: " + participantId);
            } else {
                Log.d(TAG, "getParticipantById: Participante não encontrado, ID: " + participantId);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getParticipantById: Erro ao obter índice de coluna para Participante. Verifique a definição da tabela. " + e.getMessage(), e);
            participant = null; // Indica que houve um erro na estrutura da tabela
        } catch (Exception e) {
            Log.e(TAG, "getParticipantById: Erro geral ao buscar participante por ID: " + e.getMessage(), e);
            participant = null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return participant;
    }

    public Participant getParticipantByQrCode(String qrCodeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Participant participant = null;
        try {
            cursor = db.query(TABLE_PARTICIPANTS,
                    new String[]{COLUMN_PARTICIPANT_ID, COLUMN_PARTICIPANT_NAME, COLUMN_PARTICIPANT_EMAIL, COLUMN_PARTICIPANT_EVENT_ID_FK, COLUMN_PARTICIPANT_QRCODE_ID},
                    COLUMN_PARTICIPANT_QRCODE_ID + "=?", new String[]{qrCodeId}, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                participant = new Participant();
                // Usando getColumnIndexOrThrow para segurança
                participant.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_ID)));
                participant.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_NAME)));
                participant.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_EMAIL)));
                participant.setEventId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_EVENT_ID_FK)));
                participant.setQrCodeId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_QRCODE_ID)));
                Log.d(TAG, "getParticipantByQrCode: Participante encontrado por QR Code: " + qrCodeId + ", Event ID do BD: " + participant.getEventId());
            } else {
                Log.d(TAG, "getParticipantByQrCode: Participante não encontrado por QR Code: " + qrCodeId);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getParticipantByQrCode: Erro ao obter índice de coluna para Participante. Verifique a definição da tabela. " + e.getMessage(), e);
            participant = null; // Indica que houve um erro na estrutura da tabela
        } catch (Exception e) {
            Log.e(TAG, "getParticipantByQrCode: Erro geral ao buscar participante por QR Code: " + e.getMessage(), e);
            participant = null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return participant;
    }

    public List<Participant> getAllParticipants() {
        List<Participant> participantList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PARTICIPANTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    Participant participant = new Participant();
                    try {
                        // Usando getColumnIndexOrThrow para segurança
                        participant.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_ID)));
                        participant.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_NAME)));
                        participant.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_EMAIL)));
                        participant.setEventId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_EVENT_ID_FK)));
                        participant.setQrCodeId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_QRCODE_ID)));
                        participantList.add(participant);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "getAllParticipants: Erro ao obter índice de coluna para Participante. Verifique a definição da tabela. " + e.getMessage(), e);
                        // Não adiciona o participante se houver erro em uma coluna
                    }
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "getAllParticipants: " + participantList.size() + " participantes encontrados.");
        } catch (Exception e) {
            Log.e(TAG, "getAllParticipants: Erro geral ao buscar todos os participantes: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return participantList;
    }

    // --- Métodos para Eventos ---
    public long addEvent(String eventName, String eventDate, long organizerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, eventName);
        values.put(COLUMN_EVENT_DATE, eventDate);
        values.put(COLUMN_EVENT_ORGANIZER_ID_FK, organizerId);
        values.put(COLUMN_EVENT_DESCRIPTION, ""); // Defina uma descrição padrão ou torne-a nullable no schema
        long newRowId = -1;
        try {
            newRowId = db.insertOrThrow(TABLE_EVENTS, null, values);
            Log.d(TAG, "addEvent: Evento adicionado, ID: " + newRowId + ", Organizador ID: " + organizerId);
        } catch (Exception e) {
            Log.e(TAG, "addEvent: Erro ao adicionar evento: " + e.getMessage(), e);
            newRowId = -1;
        } finally {
            db.close();
        }
        return newRowId;
    }

    public Event getEventById(long eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Event event = null;
        try {
            cursor = db.query(TABLE_EVENTS,
                    new String[]{COLUMN_EVENT_ID, COLUMN_EVENT_NAME, COLUMN_EVENT_DATE, COLUMN_EVENT_ORGANIZER_ID_FK, COLUMN_EVENT_DESCRIPTION},
                    COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(eventId)}, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                event = new Event();
                // Usando getColumnIndexOrThrow para segurança
                event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID)));
                event.setEventName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME)));
                event.setEventDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DATE)));
                event.setOrganizerId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ORGANIZER_ID_FK)));
                event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DESCRIPTION)));
                Log.d(TAG, "getEventById: Evento encontrado, ID: " + eventId);
            } else {
                Log.d(TAG, "getEventById: Evento não encontrado, ID: " + eventId);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getEventById: Erro ao obter índice de coluna para Evento. Verifique a definição da tabela. " + e.getMessage(), e);
            event = null;
        } catch (Exception e) {
            Log.e(TAG, "getEventById: Erro geral ao buscar evento por ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return event;
    }

    public List<Event> getEventsByOrganizerId(long organizerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Event> events = new ArrayList<>();
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + COLUMN_EVENT_ORGANIZER_ID_FK + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(organizerId)});

            if (cursor.moveToFirst()) {
                do {
                    Event event = new Event();
                    try {
                        // Usando getColumnIndexOrThrow para segurança
                        event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID)));
                        event.setEventName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME)));
                        event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DESCRIPTION)));
                        event.setEventDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DATE)));
                        event.setOrganizerId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ORGANIZER_ID_FK)));
                        events.add(event);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "getEventsByOrganizerId: Erro ao obter índice de coluna para Evento. Verifique a definição da tabela. " + e.getMessage(), e);
                    }
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "getEventsByOrganizerId: Nenhum evento encontrado para o organizador: " + organizerId);
            }
        } catch (Exception e) {
            Log.e(TAG, "getEventsByOrganizerId: Erro geral ao buscar eventos por ID do organizador: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return events;
    }

    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EVENTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    Event event = new Event();
                    try {
                        // Usando getColumnIndexOrThrow para segurança
                        event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID)));
                        event.setEventName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME)));
                        event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DESCRIPTION)));
                        event.setEventDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DATE)));
                        event.setOrganizerId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ORGANIZER_ID_FK)));
                        eventList.add(event);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "getAllEvents: Erro ao obter índice de coluna para Evento. Verifique a definição da tabela. " + e.getMessage(), e);
                    }
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "getAllEvents: " + eventList.size() + " eventos encontrados.");
        } catch (Exception e) {
            Log.e(TAG, "getAllEvents: Erro geral ao buscar todos os eventos: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return eventList;
    }

    // --- Métodos para CheckIns (MODIFICADOS E NOVOS) ---

    // NOVO MÉTODO: Adicionar check-in com tipo (Entrada/Saída)
    public long addCheckInWithType(long participantId, long eventId, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHECKIN_PARTICIPANT_ID_FK, participantId);
        values.put(COLUMN_CHECKIN_EVENT_ID_FK, eventId);
        values.put(COLUMN_CHECKIN_TIMESTAMP, System.currentTimeMillis()); // Armazena timestamp em milissegundos
        values.put(COLUMN_CHECKIN_TYPE, type);

        long newRowId = -1;
        try {
            newRowId = db.insertOrThrow(TABLE_CHECKINS, null, values);
            Log.d(TAG, "addCheckInWithType: Check-in de " + type + " para participante " + participantId + " no evento " + eventId + " adicionado com ID: " + newRowId);
        } catch (SQLiteConstraintException e) {
            Log.e(TAG, "addCheckInWithType: Erro de restrição ao adicionar check-in (possivelmente FK inválida): " + e.getMessage());
            newRowId = -1;
        } catch (Exception e) {
            Log.e(TAG, "addCheckInWithType: Erro ao adicionar check-in: " + e.getMessage(), e);
            newRowId = -1;
        } finally {
            db.close();
        }
        return newRowId;
    }

    // NOVO MÉTODO: Obter o último tipo de check-in para um participante em um evento
    public String getLastCheckInType(long participantId, long eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String type = null;
        String query = "SELECT " + COLUMN_CHECKIN_TYPE +
                " FROM " + TABLE_CHECKINS +
                " WHERE " + COLUMN_CHECKIN_PARTICIPANT_ID_FK + " = ? AND " +
                COLUMN_CHECKIN_EVENT_ID_FK + " = ?" +
                " ORDER BY " + COLUMN_CHECKIN_TIMESTAMP + " DESC LIMIT 1"; // Último registro

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{
                    String.valueOf(participantId),
                    String.valueOf(eventId)
            });

            if (cursor != null && cursor.moveToFirst()) {
                type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHECKIN_TYPE));
            }
            Log.d(TAG, "getLastCheckInType: Último tipo de check-in para participante " + participantId + " no evento " + eventId + ": " + (type != null ? type : "Nenhum"));
        } catch (Exception e) {
            Log.e(TAG, "getLastCheckInType: Erro ao buscar último tipo de check-in: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return type;
    }

    // MODIFICADO: getCheckInsForEvent agora retorna uma lista de AttendanceRecord (novo modelo)
    public List<AttendanceRecord> getCheckInsForEvent(long eventId) {
        List<AttendanceRecord> attendanceList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " +
                "T1." + COLUMN_CHECKIN_ID + ", " +
                "T2." + COLUMN_PARTICIPANT_NAME + ", " +
                "T2." + COLUMN_PARTICIPANT_EMAIL + ", " +
                "T1." + COLUMN_CHECKIN_TIMESTAMP + ", " +
                "T1." + COLUMN_CHECKIN_TYPE + // Seleciona a nova coluna
                " FROM " + TABLE_CHECKINS + " AS T1 " +
                " INNER JOIN " + TABLE_PARTICIPANTS + " AS T2 ON T1." + COLUMN_CHECKIN_PARTICIPANT_ID_FK + " = T2." + COLUMN_PARTICIPANT_ID +
                " WHERE T1." + COLUMN_CHECKIN_EVENT_ID_FK + " = ?" +
                " ORDER BY T1." + COLUMN_CHECKIN_TIMESTAMP + " ASC"; // Ordena por data e hora

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(eventId)});

            if (cursor.moveToFirst()) {
                do {
                    try {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_NAME));
                        String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_EMAIL));
                        // Timestamp é um INTEGER no banco, guardando milissegundos.
                        // Se você estiver exibindo no formato "DD/MM/YYYY HH:MM:SS", precisará formatar isso na UI.
                        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHECKIN_TIMESTAMP));
                        String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHECKIN_TYPE));

                        attendanceList.add(new AttendanceRecord(name, email, timestamp, type)); // Adiciona o tipo ao record
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "getCheckInsForEvent: Erro ao obter índice de coluna para Check-in. Verifique a definição da tabela. " + e.getMessage(), e);
                    }
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "getCheckInsForEvent: " + attendanceList.size() + " registros de check-in encontrados para evento " + eventId);
        } catch (Exception e) {
            Log.e(TAG, "getCheckInsForEvent: Erro geral ao buscar check-ins por evento: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return attendanceList;
    }

    public String getEventNameById(long eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String eventName = null;
        try {
            cursor = db.query(TABLE_EVENTS, new String[]{COLUMN_EVENT_NAME},
                    COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(eventId)},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                eventName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME));
                Log.d(TAG, "getEventNameById: Nome do evento encontrado para ID " + eventId + ": " + eventName);
            } else {
                Log.d(TAG, "getEventNameById: Nenhum nome de evento encontrado para ID: " + eventId);
            }
        } catch (Exception e) {
            Log.e(TAG, "getEventNameById: Erro ao buscar nome do evento por ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return eventName;
    }

    // --- Métodos de Exclusão ---
    public boolean deleteEvent(long eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        try {
            rowsAffected = db.delete(TABLE_EVENTS, COLUMN_EVENT_ID + " = ?",
                    new String[]{String.valueOf(eventId)});
            Log.d(TAG, "deleteEvent: Evento ID " + eventId + " excluído. Linhas afetadas: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "deleteEvent: Erro ao excluir evento ID " + eventId + ": " + e.getMessage(), e);
        } finally {
            db.close();
        }
        return rowsAffected > 0;
    }

    @Override
    public synchronized void close() {
        super.close();
    }
}