package com.heartrisk.model;

import java.util.regex.Pattern;

/**
 * Abstract base class of every person in the system.
 *
 * <p><b>OOAD: Inheritance and abstraction.</b> {@link Patient} and {@link Doctor} extend this
 * class and must provide their own {@link #getRole()} (polymorphism). Fields are private and
 * validated in the constructor (encapsulation).</p>
 */
public abstract class User {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s,\"]+@[^@\\s,\"]+\\.[^@\\s,\"]+$");
    private static final int MAX_NAME_LENGTH = 100;

    private final String id;
    private final String name;
    private final String email;

    /**
     * Creates a validated user.
     *
     * @param id    unique, non-blank identifier
     * @param name  display name (non-blank, at most 100 characters, single line)
     * @param email e-mail address in a simple {@code local@domain.tld} form
     * @throws IllegalArgumentException if any argument is invalid
     */
    protected User(String id, String name, String email) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH
                || name.indexOf('\n') >= 0 || name.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("name must be 1-100 characters on a single line");
        }
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("email is not a valid address");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.email = email.trim();
    }

    /** @return unique identifier */
    public String getId() {
        return id;
    }

    /** @return display name */
    public String getName() {
        return name;
    }

    /** @return e-mail address */
    public String getEmail() {
        return email;
    }

    /**
     * Role of this user in the system; implemented differently by each subclass.
     *
     * @return role name, for example {@code "PATIENT"}
     */
    public abstract String getRole();
}
