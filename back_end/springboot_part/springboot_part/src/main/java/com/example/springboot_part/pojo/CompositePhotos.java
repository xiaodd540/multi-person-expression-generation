package com.example.springboot_part.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString()
public class CompositePhotos {
    private Integer CompositePhotoId;
    private int PhotoId;
    private String CompositePhotoPath;

    public CompositePhotos() {}

    public CompositePhotos(int CompositedPhotoId, int PhotoId, String CompositedPhotoPath) {
        this.CompositePhotoId = CompositedPhotoId;
        this.PhotoId = PhotoId;
        this.CompositePhotoPath = CompositedPhotoPath;
    }
}
