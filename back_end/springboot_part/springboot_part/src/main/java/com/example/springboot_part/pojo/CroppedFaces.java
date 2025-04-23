package com.example.springboot_part.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString()
public class CroppedFaces {
    private Integer FaceId;
    private int PhotoId;
    private String FacePath;
    private double X;
    private double Y;
    private double Width;
    private double Height;

    public CroppedFaces() {}

    public CroppedFaces(int FaceId, int PhotoId, String FacePath, double X, double Y, double Width, double Height) {
        this.FaceId = FaceId;
        this.PhotoId = PhotoId;
        this.FacePath = FacePath;
        this.X = X;
        this.Y = Y;
        this.Width = Width;
        this.Height = Height;
    }
}
