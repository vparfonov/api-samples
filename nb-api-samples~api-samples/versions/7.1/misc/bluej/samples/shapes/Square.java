import java.awt.*;

/**
 * A square that can be manipulated and that draws itself on a canvas.
 * 
 * @author	Michael Kolling
 * @version 1.0  (15 July 2000)
 */

public class Square
{
    private int size;
	private int xPosition;
	private int yPosition;
	private String color;

    /**
     * Create a new square at default position with default color.
     */
    public Square()
    {
		size = 30;
		xPosition = 60;
		yPosition = 50;
		color = "red";
		draw();
    }

    /**
     * Move the square a few pixels to the right.
     */
    public void moveRight()
    {
		moveHorizontal(20);
    }

    /**
     * Move the square a few pixels to the left.
     */
    public void moveLeft()
    {
		moveHorizontal(-20);
    }

    /**
     * Move the square a few pixels up.
     */
    public void moveUp()
    {
		moveVertical(-20);
    }

    /**
     * Move the square a few pixels down.
     */
    public void moveDown()
    {
		moveVertical(20);
    }

    /**
     * Move the square horizontally by 'distance' pixels.
     */
    public void moveHorizontal(int distance)
    {
		erase();
		xPosition += distance;
		draw();
    }

    /**
     * Move the square vertically by 'distance' pixels.
     */
    public void moveVertical(int distance)
    {
		erase();
		yPosition += distance;
		draw();
    }

    /**
     * Slowly move the square horizontally by 'distance' pixels.
     */
    public void slowMoveHorizontal(int distance)
    {
		int delta;

		if(distance < 0) 
		{
			delta = -1;
			distance = -distance;
		}
		else 
		{
			delta = 1;
		}

		for(int i = 0; i < distance; i++)
		{
			erase();
			xPosition += delta;
			draw();
		}
    }

    /**
     * Slowly move the square vertically by 'distance' pixels.
     */
    public void slowMoveVertical(int distance)
    {
		int delta;

		if(distance < 0) 
		{
			delta = -1;
			distance = -distance;
		}
		else 
		{
			delta = 1;
		}

		for(int i = 0; i < distance; i++)
		{
			erase();
			yPosition += delta;
			draw();
		}
    }

    /**
     * Change the size to the new size (in pixels). Size must be >= 0.
     */
    public void changeSize(int newSize)
    {
		erase();
		size = newSize;
		draw();
    }

    /**
     * Change the color. Valid colors are "red", "yellow", "blue", "green",
	 * "magenta" and "black".
     */
    public void changeColor(String newColor)
    {
		color = newColor;
		draw();
    }

	/*
	 * Draw the square with current specifications on screen.
	 */
	private void draw()
	{
		Canvas canvas = Canvas.getCanvas();
		canvas.setForegroundColour(color);
		canvas.fill(new Rectangle(xPosition, yPosition, size, size));
		canvas.wait(10);
	}

	/*
	 * Erase the square on screen.
	 */
	private void erase()
	{
		Canvas canvas = Canvas.getCanvas();
		canvas.erase(new Rectangle(xPosition, yPosition, size, size));
	}
}
