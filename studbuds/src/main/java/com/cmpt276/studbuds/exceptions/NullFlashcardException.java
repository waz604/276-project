package com.cmpt276.studbuds.exceptions;

public class NullFlashcardException extends RuntimeException {

    private final long deckId;

    public NullFlashcardException(long deckId) {
        this.deckId = deckId;
    }

    public long getDeckId() {
        return this.deckId;
    }
    
}