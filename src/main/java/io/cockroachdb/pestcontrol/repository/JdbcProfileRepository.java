package io.cockroachdb.pestcontrol.repository;

import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

public class JdbcProfileRepository implements ProfileRepository {
    public static final Calendar tzUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    private static final String JSON_DATA =
            """
                    [
                      {
                        "_id": "670d522e1ad3f85502f44ecd",
                        "index": 0,
                        "guid": "5a44c0f0-a7a4-4cbc-8bdd-7d550d1e8d2a",
                        "isActive": true,
                        "balance": "$3,972.10",
                        "picture": "http://placehold.it/32x32",
                        "age": 31,
                        "eyeColor": "brown",
                        "name": "Kristi Blackburn",
                        "gender": "female",
                        "company": "INQUALA",
                        "email": "kristiblackburn@inquala.com",
                        "phone": "+1 (897) 482-3250",
                        "address": "793 Columbia Street, Westboro, Illinois, 2347",
                        "about": "Amet aliquip do cupidatat ex incididunt fugiat. Deserunt in pariatur ea do. Occaecat tempor do ad ut do Lorem non mollit occaecat enim occaecat. In non officia tempor amet pariatur est qui pariatur occaecat. Sunt sunt veniam reprehenderit commodo magna id. Consectetur ut ipsum mollit incididunt in amet sunt elit eiusmod irure ex. Pariatur aliquip aliqua voluptate est occaecat irure cillum esse.\\r\\n",
                        "registered": "2015-07-13T02:00:27 -02:00",
                        "latitude": -69.276752,
                        "longitude": 36.555489,
                        "tags": [
                          "ad",
                          "commodo",
                          "do",
                          "pariatur",
                          "non",
                          "tempor",
                          "consequat"
                        ],
                        "friends": [
                          {
                            "id": 0,
                            "name": "Edwards Richmond"
                          },
                          {
                            "id": 1,
                            "name": "Cynthia Potter"
                          },
                          {
                            "id": 2,
                            "name": "Yvonne Fowler"
                          }
                        ],
                        "greeting": "Hello, Kristi Blackburn! You have 7 unread messages.",
                        "favoriteFruit": "apple"
                      }
                    ]
                    """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcProfileRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean isSchemaReady() {
        return !jdbcTemplate.queryForList(
                "select table_name from [show tables] where table_name='pc_user_profile'").isEmpty();
    }

    @Override
    public ProfileEntity insertProfileSingleton() {
        ProfileEntity profile = new ProfileEntity();
        profile.setExpireAt(LocalDateTime.now());
        profile.setVersion(0);
        profile.setProfile(JSON_DATA);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pc_user_profile (expire_at,payload,version) "
                    + "VALUES (?,?,?) returning id::uuid",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setObject(1, profile.getExpireAt());
            ps.setCharacterStream(2, new StringReader(profile.getProfile()));
            ps.setInt(3, profile.getVersion());
            return ps;
        }, keyHolder);

        profile.setId(keyHolder.getKeyAs(UUID.class));

        return profile;
    }

    @Override
    public List<ProfileEntity> insertProfileBatch(int batchSize) {
        List<ProfileEntity> profileBatch = new ArrayList<>();

        IntStream.rangeClosed(1, batchSize).forEach(value -> {
            ProfileEntity profile = new ProfileEntity();
            profile.setId(UUID.randomUUID());
            profile.setExpireAt(LocalDateTime.now());
            profile.setVersion(0);
            profile.setProfile(JSON_DATA);
            profileBatch.add(profile);
        });

        jdbcTemplate.update(
                "INSERT INTO pc_user_profile (id,expire_at,payload,version) "
                + "select unnest(?) as id, "
                + "       unnest(?) as expire_at, "
                + "       unnest(?) as payload, "
                + "       unnest(?) as version",
                ps -> {
                    List<UUID> id = new ArrayList<>();
                    List<LocalDateTime> expire_at = new ArrayList<>();
                    List<String> payload = new ArrayList<>();
                    List<Integer> version = new ArrayList<>();

                    profileBatch.forEach(profile -> {
                        id.add(profile.getId());
                        expire_at.add(profile.getExpireAt());
                        payload.add(profile.getProfile());
                        version.add(profile.getVersion());
                    });

                    ps.setArray(1, ps.getConnection()
                            .createArrayOf("UUID", id.toArray()));
                    ps.setArray(2, ps.getConnection()
                            .createArrayOf("TIMESTAMP", expire_at.toArray()));
                    ps.setArray(3, ps.getConnection()
                            .createArrayOf("JSONB", payload.toArray()));
                    ps.setArray(4, ps.getConnection()
                            .createArrayOf("INT", version.toArray()));
                });

        return profileBatch;
    }

    @Override
    public void updateProfile(ProfileEntity profile) {
        final String sql = "UPDATE pc_user_profile SET expire_at=?,payload=?,version=? "
                           + "WHERE id=? and version=?";

        int rows = jdbcTemplate.update(sql,
                ps -> {
                    ps.setObject(1, profile.getExpireAt());
                    ps.setCharacterStream(2, new StringReader(profile.getProfile()));
                    ps.setInt(3, profile.getVersion());
                    ps.setObject(4, profile.getId());
                    ps.setInt(5, profile.getVersion());
                });

        if (rows != 1) {
            throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sql, 1, rows);
        }
    }

    @Override
    public void updateRandomProfile() {
        findByRandomId().ifPresent(this::updateProfile);
    }

    @Override
    public void deleteProfileById(UUID id) {
        jdbcTemplate.update("DELETE from pc_user_profile WHERE id=? and version=0",
                ps -> ps.setObject(1, id));
    }

    @Override
    public void deleteRandomProfile() {
        findByRandomId().ifPresent(profileEntity ->
                deleteProfileById(profileEntity.getId()));
    }

    @Override
    public List<ProfileEntity> findAll(int limit) {
        return jdbcTemplate.query("SELECT * FROM pc_user_profile limit " + limit,
                profileRowMapper());
    }

    @Override
    public Optional<ProfileEntity> findFirst(boolean followerRead) {
        return jdbcTemplate
                .query(followerRead ? "SELECT * FROM pc_user_profile "
                                      + "as of system time follower_read_timestamp() order by id limit 1" :
                                "SELECT * FROM pc_user_profile order by id limit 1",
                        profileRowMapper())
                .stream()
                .findFirst();
    }

    @Override
    public Optional<ProfileEntity> findByNextId(UUID id, boolean followerRead) {
        return jdbcTemplate
                .query(followerRead ? "SELECT * FROM pc_user_profile "
                                      + "as of system time follower_read_timestamp() where id > ? order by id limit 1" :
                                "SELECT * FROM pc_user_profile where id > ? order by id limit 1",
                        profileRowMapper(), id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<ProfileEntity> findByRandomId() {
        return jdbcTemplate
                .query("SELECT * FROM pc_user_profile ORDER BY random() limit 1",
                        profileRowMapper())
                .stream()
                .findFirst();
    }

    @Override
    public Optional<ProfileEntity> findById(UUID id) {
        return jdbcTemplate
                .query("SELECT * FROM pc_user_profile "
                       + "WHERE (id,version) IN (?,?)",
                        profileRowMapper(),
                        id, 0)
                .stream()
                .findFirst();
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.execute("delete from pc_user_profile where 1=1");
    }

    private RowMapper<ProfileEntity> profileRowMapper() {
        return (rs, rowNum) -> {
            ProfileEntity profile = new ProfileEntity();
            profile.setId(rs.getObject("id", UUID.class));
            profile.setVersion(rs.getInt("version"));

            Timestamp ts = rs.getTimestamp("expire_at", tzUTC);
            profile.setExpireAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts.getTime()), ZoneOffset.UTC));

            String payload = rs.getString("payload");
            profile.setProfile(payload);

            return profile;
        };
    }
}
