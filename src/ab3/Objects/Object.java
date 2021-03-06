package ab3.Objects;

import ab3.Datatypes.*;
import ab3.Primary.Themes;
import lenz.opengl.ShaderProgram;
import lenz.opengl.Texture;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public abstract class Object {
    private ShaderProgram shaderProgram;
    private float[] edges;
    private Vector3 position;
    private Matrix4 mat;
    private float angleX = 0;
    private float angleY = 0;
    private float currentAngleX = 0;
    private float currentAngleY = 0;

    private float rotationSpeed = 0.1f;

    public enum Transformation {ROTATE_X, ROTATE_Y, ROTATE_XY}

    private Transformation transformation;

    private int vaold;


    void init(float[] edges, Vector3 position, Transformation transformation, float[] uvCoordinates, String shader, Themes texture) {
        shaderProgram = new ShaderProgram(shader);
        glUseProgram(shaderProgram.getId());
        this.edges = edges;
        this.position = position;
        this.transformation = transformation;
        this.mat = new Matrix4();
        //----------------------------------------------------------------------------------------------------
        vaold = glGenVertexArrays();
        glBindVertexArray(vaold);
        //----------------------------------------------------------------------------------------------------
        int vbold = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbold);
        glBufferData(GL_ARRAY_BUFFER, edges, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        //----------------------------------------------------------------------------------------------------
        float[] normals = getNormals();
        vbold = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbold);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);
        //----------------------------------------------------------------------------------------------------
        vbold = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbold);
        glBufferData(GL_ARRAY_BUFFER, uvCoordinates, GL_STATIC_DRAW);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(2);
        //----------------------------------------------------------------------------------------------------
        Texture textureID = new Texture(texture.toString());
        glBindTexture(GL_TEXTURE_2D, textureID.getId());
        glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        //----------------------------------------------------------------------------------------------------
        glEnable(GL_DEPTH_TEST); // z-Buffer aktivieren
        glEnable(GL_CULL_FACE); // backface culling aktivieren
    }

    public void update() {
        transform(transformation);
        this.mat.translate(position.x(), position.y(), position.z());
    }

    public void render() {
        glBindVertexArray(vaold);

        int loc = glGetUniformLocation(shaderProgram.getId(), "mat");
        glUniformMatrix4fv(loc, false, mat.getValuesAsArray());
        glDrawArrays(GL_TRIANGLES, 0, edges.length / 3);

        glBindVertexArray(0);
    }
    //----------------------------------------------------------------------------------------------------

    private float[] getNormals() {
        float[] normals = new float[edges.length];
        for (int i = 0; i < edges.length; i += 9) {
            Vector3 firstVector = Vector3.getVector(edges[i], edges[i + 1], edges[i + 2], edges[i + 3], edges[i + 4], edges[i + 5]);
            Vector3 secondVector = Vector3.getVector(edges[i], edges[i + 1], edges[i + 2], edges[i + 6], edges[i + 7], edges[i + 8]);
            Vector3 normal = Vector3.getNormal(firstVector, secondVector);
            for (int j = 0; j < 3; j++) {
                normals[i + j * 3] = normal.getAsArray()[0];
                normals[i + j * 3 + 1] = normal.getAsArray()[1];
                normals[i + j * 3 + 2] = normal.getAsArray()[2];
            }
        }
        return normals;
    }

    //----------------------------------------------------------------------------------------------------

    public void setAngleX(float angleX) {
        this.angleX = angleX;
    }

    public void setAngleY(float angleY) {
        this.angleY = angleY;
    }

    private void transform(Transformation transformation) {
        this.mat = new Matrix4();
        switch (transformation) {
            case ROTATE_X:
                currentAngleX = (currentAngleX + rotationSpeed) % 360;
                break;
            case ROTATE_Y:
                currentAngleY = (currentAngleY + rotationSpeed) % 360;
                break;
            case ROTATE_XY:
                currentAngleX = (currentAngleX + rotationSpeed) % 360;
                currentAngleY = (currentAngleY + rotationSpeed) % 360;
                break;
        }
        float nextAngleX = (currentAngleX + angleX) % 360;
        float nextAngleY = (currentAngleY + angleY) % 360;
        this.mat.rotateX(nextAngleX).rotateY(nextAngleY);
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }



}
