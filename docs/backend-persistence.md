# Backend persistence layer

Java 21 / Spring Boot / Spring Data JPA. Existing package: com.example.demo. Source directory: demo/demo/src/main/java.

The 11 public tables created in Supabase are mapped by 11 entities and repositories. Four join tables use @EmbeddedId and @MapsId. Use persisted parent entities to construct join entities. PostCategory and UserRole correspond to the existing database CHECK constraints. DTOs must never serialize User.passwordHash.

Use the existing server-side datasource environment variables. For schema validation set spring.jpa.hibernate.ddl-auto=validate via environment variable SPRING_JPA_HIBERNATE_DDL_AUTO=validate; do not use create or create-drop on Supabase. Database credentials remain outside Git. Existing application.properties not modified.

Services must run write operations in transactions, hash passwords with Spring Security password encoder, check roles and ownership, synchronize comments.empathy_count with comment_likes (or compute from joins), and increment post views atomically using incrementViewCount. No visit-dedup table is required. Favorite ranking counts current favorite rows and filters before sorting. Entity timestamps are initialized in Java and updated by @PreUpdate; database defaults also exist. PostgreSQL indexes and constraints remain database-managed.

Controllers, services, DTOs, authentication and KMAS ingestion are not implemented in this step. Gradle compilation and schema validation against live DB must be run with JDK 21 and configured datasource.
