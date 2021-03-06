package com.theironyard.controllers;

import com.theironyard.entities.Photo;
import com.theironyard.entities.User;
import com.theironyard.services.PhotoRepository;
import com.theironyard.services.UserRepository;
import com.theironyard.utils.PasswordHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jessicahuffstutler on 11/17/15.
 */
@RestController
public class IronGramController {
    @Autowired
    UserRepository users;

    @Autowired
    PhotoRepository photos;

    @RequestMapping("/login")
    public User login(HttpSession session, HttpServletResponse response, String username, String password) throws Exception {
        User user = users.findOneByUsername(username); //look to see if user already exists in database

        if(user == null) {
            user = new User();
            user.username = username;
            user.password = PasswordHash.createHash(password);
            users.save(user);
        } else if (!PasswordHash.validatePassword(password, user.password)) {
            throw new Exception("Wrong password...");
        }

        session.setAttribute("username", username);

        response.sendRedirect("/");

        return user; //return the user object with username and password
    }

    @RequestMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response) throws IOException { //void because we don't need to return anything but we will need to redirect, will add later
        session.invalidate();
        response.sendRedirect("/"); //if they're hitting that route using AJAX we don't need to redirect, but if they are hitting it using basic html forms like we are, we need to redirect
    }

    @RequestMapping("/user")
    public User user(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return null;
        }
        return users.findOneByUsername(username);
    }

    @RequestMapping("/upload")
    public Photo upload(
            HttpSession session,
            HttpServletResponse response,
            String receiver,
            int deleteTime, //could have @RequestParam(defaultValue = "10") to auto decide 10 seconds for the user that can be changed
            boolean isPublic,
            MultipartFile photo
    ) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new Exception("Not Logged In.");
        }

        User senderUser = users.findOneByUsername(username);
        User receiverUser = users.findOneByUsername(receiver); //looking up user that you typed into the input box

        if (receiverUser == null) {
            throw new Exception("Receiver name doesn't exist.");
        }

        if (!photo.getContentType().startsWith("image")) {
            throw new Exception("Only images can be uploaded.");
        }

        //grab file and save to right spot on the disc
        //hello.png => hello847598374592hello.png
        File photoFile = File.createTempFile("photo", photo.getOriginalFilename(), new File("public")); //prefix, suffix and where the file is saved
        FileOutputStream fos = new FileOutputStream(photoFile);
        fos.write(photo.getBytes()); //save all those bytes into that temporary file

        Photo p = new Photo();
        p.sender = senderUser;
        p.receiver = receiverUser;
        p.filename = photoFile.getName(); //gets file name that it randomly generated for you
        p.deleteTime = deleteTime;
        p.isPublic = isPublic;
        photos.save(p);

        response.sendRedirect("/");

        return p;
    }

    @RequestMapping("/photos")
    public List<Photo> showPhotos(HttpSession session) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new Exception("Not logged in.");
        }

        User user = users.findOneByUsername(username);

        List<Photo> photoList = photos.findByReceiver(user);

        for (Photo p : photoList) {
            if(p.viewTime == null) { //if it's null, then it's the first time we are looking at the photo
                p.viewTime = LocalDateTime.now(); //so set the viewtime to now.
                photos.save(p); //save it to the database
                //below: delete file with thread technique or timer technique
                // waitToDelete(p, 10); //from "waitToDelete" method below alt to
            } else if (p.viewTime.isBefore(LocalDateTime.now().minusSeconds(p.deleteTime))) { //if y < (x-10), delete
                photos.delete(p);
                File tempImg = new File("public", p.filename);
                tempImg.delete();
            }
        }

        return photos.findByReceiver(user); //this is just data about the photo, not the photo itself.
    }

    public void waitToDelete(Photo photo, int seconds) {
        //Thread technique
        /*
        Thread t = new Thread(() -> { //takes lambda, 0 arguments
            try {
                Thread.sleep(seconds * 1000); //surround with try catch, because you cant add exception to method signature
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            photos.delete(photo);
            File f = new File("public", photo.filename);
            f.delete();
        });
        t.start();
        */

        //Timer technique
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                photos.delete(photo);
                File f = new File("public", photo.filename);
                f.delete();
            }
        }, seconds * 1000);
    }

    //public photos that a specific user sent
    @RequestMapping("/public-photos")
    public List<Photo> publicPhotos(String username) throws Exception { //list of Photo objects
        //you don't need to be logged in because this is meant to be public

        User user = users.findOneByUsername(username); //get user from database

        /* Notes from class
        List<Photo> photoList = photos.findBySender(user); //get images from database

        //filter out the private photos
        */

        ArrayList<Photo> publicList = new ArrayList();
        for(Photo p : photos.findBySender(user)){
            if(p.isPublic){
                publicList.add(p);
            }
        }

        return publicList;
    }
}
