package io.cockroachdb.pestcontrol.domain;

public enum WorkloadType {
    profile_insert("Profile singleton insert",
            "A single insert statement."),
    profile_batch_insert("Profile batch insert",
            "A single batch of 32 insert statements."),
    profile_update("Profile point read and update",
            "A single point lookup read followed by an update."),
    profile_delete("Profile point read and delete",
            "A single point lookup read followed by a delete."),
    profile_read("Profile point read",
            "A single authoritative point lookup read."),
    profile_follower_read("Profile follower read",
            "A single historical point lookup read."),
    profile_scan("Profile full scan",
            "A single full table scan."),
    select_one("Select one",
            "A basic 'select 1' statement."),
    random_wait("Random wait",
            "A random wait not touching the DB."),
    fixed_wait("Fixed wait",
            "A fixed wait not touching the DB.");

    private final String displayValue;

    private final String description;

    WorkloadType(String displayValue, String description) {
        this.displayValue = displayValue;
        this.description = description;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getDescription() {
        return description;
    }
}
