package com.example.zadanie_20481024;

import java.util.List;

/**
 * This class is immutable.
 */
class FullPost {
    private final int postId;
    private final String title, body, author, authorEmail;
    private final List<Comment> comments;

    FullPost(int postId, String title, String body, String author, String authorEmail, List<Comment> comments) {
        this.postId = postId;
        this.title = title;
        this.body = body;
        this.author = author;
        this.authorEmail = authorEmail;
        this.comments = comments;
    }


    static class Comment{
        private final String name, email, body;

        Comment(String name, String email, String body) {
            this.name = name;
            this.email = email;
            this.body = body;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getBody() {
            return body;
        }
    }

    public int getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public List<Comment> getComments() {
        return comments;
    }
}
