package de.tum.cit.fop.maze.essentials;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Null;
import org.jetbrains.annotations.NotNull;

/**
 * A button with an image positioned to the left and text positioned in the center of the button.
 * Paddings can be set for the image and the text.
 */
public class AlignableImageTextButton extends Button {
    private final Image image;
    private final Label label;
    private final float imageScale;
    private float imagePadding = 0f;
    private float imageTopPadding = 0f;
    private float labelPadding = 0f;
    private float labelTopPadding = 0f;
    private ImageTextButtonStyle style;

    /**
     * Creates a new button with the provided text, style and image scale.
     * @param text The text to display on the button.
     * @param style The style of the image on the button.
     * @param imageScale The scale of the image on the button.
     */
    public AlignableImageTextButton(@Null String text, @NotNull ImageTextButtonStyle style, Image image, @Null float imageScale) {
        super(style);
        this.style = style;
        this.imageScale = imageScale;

        defaults().space(3);
        this.image = image;
        label = new Label(text, new Label.LabelStyle(style.font, style.fontColor));

        this.image.setPosition(imagePadding, (getHeight() - image.getHeight())/2 + imageTopPadding);
        label.setPosition(getWidth()/2 + labelPadding, (getHeight() - label.getHeight())/2 + labelTopPadding);

        add(this.image);
        add(label);

        setStyle(style);

        setSize(super.getWidth(), super.getHeight());
    }

    @Override
    public void setStyle(ButtonStyle style){
        if (!(style instanceof ImageTextButtonStyle)) throw new IllegalArgumentException("style must be a ImageTextButtonStyle.");
        this.style = (ImageTextButtonStyle)style;
        super.setStyle(style);

        if (image != null) updateImage();

        if (label != null) {
            ImageTextButtonStyle textButtonStyle = (ImageTextButtonStyle)style;
            Label.LabelStyle labelStyle = label.getStyle();
            labelStyle.font = textButtonStyle.font;
            labelStyle.fontColor = getFontColor();
            label.setStyle(labelStyle);
        }
    }


    @Override
    public void draw (Batch batch, float parentAlpha) {
        updateImage();
        updateLabel();
        label.getStyle().fontColor = getFontColor();
        super.draw(batch, parentAlpha);
    }

    /**
     * Sets the image drawable based on the provided image in the {@code style} attribute.
     * Sets the image height and width based on the {@code imageScale} attribute.
     */
    protected void updateImage(){
        image.setHeight(image.getDrawable().getMinHeight() * imageScale);
        image.setWidth(image.getDrawable().getMinWidth() * imageScale);
        image.setPosition(imagePadding, (getHeight() - image.getHeight())/2 + imageTopPadding);
    }

    /**
     * Updates the label position based on the {@code labelPadding} attribute.
     */
    protected void updateLabel(){
        //label.getPrefWidth() returns the width of the label text, which allows to center the label in the button
        label.setPosition((getWidth() - label.getPrefWidth())/2 + labelPadding, (getHeight() - label.getHeight())/2 + labelTopPadding);
    }

    public void setImagePadding(float imagePadding){
        this.imagePadding = imagePadding;
    }

    public void setImageTopPadding(float imagePadding){
        this.imageTopPadding = imagePadding;
    }

    public void setLabelPadding(float labelPadding){
        this.labelPadding = labelPadding;
    }

    public void setLabelTopPadding(float labelPadding){
        this.labelTopPadding = labelPadding;
    }

    public void setText (CharSequence text) {
        label.setText(text);
    }

    public CharSequence getText () {
        return label.getText();
    }

    public Image getImage() {
        return image;
    }

    public Label getLabel() {
        return label;
    }

    protected @Null Color getFontColor(){
        return style.fontColor;
    }
    protected @Null Drawable getImageDrawable(){
        return image.getDrawable();
    }
}
