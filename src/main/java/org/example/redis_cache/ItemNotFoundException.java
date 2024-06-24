package org.example.redis_cache;

public class ItemNotFoundException extends RuntimeException 
{
    public ItemNotFoundException() {
        super("item not found");
    }
 
    public ItemNotFoundException(String message) {
       super(message);
    }
}
