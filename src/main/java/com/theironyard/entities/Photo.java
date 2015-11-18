package com.theironyard.entities;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by jessicahuffstutler on 11/17/15.
 */
@Entity
@Table(name = "photos")
public class Photo {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    public int id;

    @Column(nullable = false)
    public int deleteTime;

    @ManyToOne
    public User sender;

    @ManyToOne
    public User receiver;

    @Column(nullable = false)
    public String filename;

    //dont put nullable = false, because it will be null until the image is viewed
    @Column
    public LocalDateTime viewTime;

    @Column(nullable = false)
    public boolean isPublic;
}
