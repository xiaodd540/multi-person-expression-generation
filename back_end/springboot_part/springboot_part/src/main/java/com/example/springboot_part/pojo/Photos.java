package com.example.springboot_part.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString()
public class Photos {
    private Integer PhotoId;
    private int UserId;
    private String PhotoPath;
    private Timestamp UploadTime;
    private String PhotosHash;
    private Status Status;

    public enum Status {Pending,Processing,Completed};

    public Photos() {}

    public Photos(int PhotoId, int UserId, String PhotoPath, Timestamp UploadTime, String PhotosHash ,Status status) {
        this.PhotoId = PhotoId;
        this.UserId = UserId;
        this.PhotoPath = PhotoPath;
        this.UploadTime = UploadTime;
        this.Status = status;
        this.PhotosHash = PhotosHash;
    }

}
