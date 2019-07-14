package com.example.zadanie_20481024;

/**
 * This class has the title of the post, it's body and id. This class is immutable.
 */
class PostSummary {
    private final String title, body;
    private final int id;

    PostSummary(int id, String title, String body) {
        this.title = title;
        this.body = body;
        this.id = id;
    }

    String getTitle() {
        return title;
    }

    String getBody() {
        return body;
    }

    int getId() {
        return id;
    }
}
