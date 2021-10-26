
/*
 * ==========================================================================================
 * AnimationViewer.java : Moves shapes around on the screen according to different paths.
 * It is the main drawing area where shapes are added and manipulated.
 * 
 * ==========================================================================================
 */

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;

class AnimationViewer extends JComponent implements Runnable {
	private Thread animationThread = null;    // the thread for animation
    private static int DELAY = 30;         // the current animation speed
    private Painter painter = new GraphicsPainter();;
    private ShapeType currentShapeType=Shape.DEFAULT_SHAPETYPE; // the current shape type,
    private PathType currentPathType=Shape.DEFAULT_PATHTYPE;  // the current path type
    private Color currentColor=Shape.DEFAULT_COLOR; // the current fill colour of a shape
    private int marginWidth=Shape.DEFAULT_MARGIN_WIDTH, marginHeight = Shape.DEFAULT_MARGIN_HEIGHT, currentWidth=Shape.DEFAULT_WIDTH, currentHeight=Shape.DEFAULT_HEIGHT;
	private String currentText=Shape.DEFAULT_TEXT;
	private ShapeType currentInnerShapeType=Shape.DEFAULT_SHAPETYPE;
    private NestedShape root;
    private NestedShape selectedNestedShape;
	private ShapeModelAdapter shapeModelAdapter;

     /** Constructor of the AnimationViewer */
    public AnimationViewer(boolean isGraphicsVersion) {
		if (isGraphicsVersion) {
			start();
			addMouseListener(new MyMouseAdapter());
		}
		int w = Shape.DEFAULT_MARGIN_WIDTH;
		int h = Shape.DEFAULT_MARGIN_HEIGHT;
		NestedShape ns =new NestedShape(w,h);
		this.root = ns;
		this.selectedNestedShape = ns;
        this.shapeModelAdapter = new ShapeModelAdapter();
		//complete this
    }
    public void createNewShape(int x, int y){
	    Shape s = root.createInnerShape(x,y,currentWidth,currentHeight,marginWidth,marginHeight,currentColor,currentText,currentShapeType,currentInnerShapeType);

	    shapeModelAdapter.insertNodeInto(s,root);
	}
	public boolean isRoot(Shape selectedNode){
	    return (selectedNode==root);
	}
	public NestedShape getRoot(){return root;}
	public ShapeModelAdapter getShapeModelAdapter(){
		return shapeModelAdapter;}
	public void addShapeNode(NestedShape selectedNode){

	    if(selectedNode==root){
	        int width = currentWidth;
	        int height = currentHeight;
	        Shape s = root.createInnerShape(0,0,width,height,currentColor,currentText,currentShapeType,currentInnerShapeType);
			shapeModelAdapter.insertNodeInto(s,root);
	    }else if(selectedNode!=root){

	        Shape s = selectedNode.createInnerShape(currentShapeType,currentInnerShapeType);
	        shapeModelAdapter.insertNodeInto(s,selectedNode);
	    }


	}
	public void removeShapeNode(Shape selectedNode){

	    shapeModelAdapter.removeNodeFromParent(selectedNode);
	}
	public void setSelectedNestedShape(NestedShape ns){
	    selectedNestedShape = ns;
	    shapeModelAdapter.fireTableStructureChanged();
	}
    //complete this
	class ShapeModelAdapter extends AbstractTableModel implements TreeModel  {
	    private ArrayList<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
	    public Object getRoot(){return root;}
	    public ShapeModelAdapter(){super();}

	    private String[] columnNames = {"Type","Width","Height"};
	    public boolean isLeaf(Object node){return !(node instanceof NestedShape);}
	    public Object getChild(Object parent, int index){
	        if(parent instanceof NestedShape &&index>=0){
				if(index<root.getSize()){
					NestedShape ns = (NestedShape)parent;
	            				return ns.getInnerShapeAt(index);
				}

	        }
	        return null;
	    }
	    public int getChildCount(Object parent){
	        int result = 0;
	        if(parent instanceof NestedShape){
	            NestedShape ns = (NestedShape)parent;


	            return ns.getSize();
	        }
	        return result;
	    }
	    public int getIndexOfChild(Object parent, Object child){
	        if(parent instanceof NestedShape){
	            NestedShape ns = (NestedShape)parent;
	            NestedShape s =  (NestedShape)ns;
	            Shape c = (Shape)child;
	            return s.indexOf(c);
	        }
	        return -1;
	    }
	    public void addTreeModelListener(final TreeModelListener tml){treeModelListeners.add(tml);}
	    public void removeTreeModelListener(final TreeModelListener tml){treeModelListeners.remove(tml);}
	    public void valueForPathChanged(TreePath path, Object newValue){}
	    public int getColumnCount(){return columnNames.length;}
	    public int getRowCount(){
	        return root.getSize();
	    }
	    public String getColumnName(int column){return columnNames[column];}
	    public Object getValueAt(int rowIndex, int columnIndex){

	        if(columnIndex==0){
	            Shape s = root.getInnerShapeAt(rowIndex);
	            return s.getClass().getName();


	        }else{
	            if(columnIndex==1){
	               Shape s = root.getInnerShapeAt(rowIndex);
	                s.getClass().getName();

			        return s.width;
	            }else{
	                Shape s = root.getInnerShapeAt(rowIndex);
	                s.getClass().getName();

			        return s.height;
	            }

	        }



	    }
	    public void insertNodeInto(Shape newChild, NestedShape parent){

	        if(newChild instanceof NestedShape){
	            NestedShape nc = (NestedShape)parent;
	            NestedShape s = (NestedShape)newChild;
	            int index = nc.indexOf(s);
	            int []childIndices = new int[index];
	            Object[]Child = new Object[index];
	            fireTreeNodesInserted(this,root.getPath(),childIndices,Child);

	            fireTableRowsInserted(index, index);
	        }

	    }
	    public void removeNodeFromParent(Shape selectedNode){
	        NestedShape parent = (NestedShape)selectedNode.getParent();
	        int index = parent.indexOf(selectedNode);
	        parent.remove(selectedNode);
	        int []childIndices = new int[index];
	        Object []children = new Object[index];
	        fireTreeNodesRemoved(this,root.getPath(),childIndices,children);
	        fireTableRowsDeleted(index,index);
	    }


		public void fireTreeNodesChanged(TreeModelEvent e) {}
		protected void fireTreeNodesRemoved(Object source, Object[] path,int[] childIndices,Object[] children) {
			final TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
			for (final TreeModelListener l : treeModelListeners)
				l.treeNodesRemoved(event);
	    }
	    protected void fireTreeNodesInserted(Object source, Object[] path,int[] childIndices,Object[] children) {
			final TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
			for (final TreeModelListener l : treeModelListeners)
				l.treeNodesInserted(event);
	    }
	    protected void fireTreeStructureChanged(final Object source, final Object[] path, final int[] childIndices, final Object[] children) {
	        final TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
	        for (final TreeModelListener l : treeModelListeners) {
	            l.treeStructureChanged(event);
	        }
		}
	}

	/** get the current width
	 * @return currentWidth - the width value */
	public int getCurrentWidth() { return currentWidth; }
	/** get the current height
	 * @return currentHeight - the height value */
	public int getCurrentHeight() { return currentHeight; }
	/** get the current fill colour
	 * @return currentColor - the fill colour value */
	public Color getCurrentColor() { return currentColor; }
    /**    move and paint all shapes within the animation area
     * @param g    the Graphics control */
    public void paintComponent(Graphics g) {
		painter.setGraphics(g);
		super.paintComponent(g);
        for (Shape currentShape: root.getAllInnerShapes()) {
            currentShape.move();
		    currentShape.draw(painter);
            currentShape.drawHandles(painter);
            currentShape.drawString(painter);
		}
    }
    public ShapeType getCurrentShapeType() { return currentShapeType; }
    public void setCurrentShapeType(int st) {
		currentShapeType = ShapeType.getShapeType(st);
	}
    public ShapeType getCurrentInnerShapeType() { return currentInnerShapeType; }
    public void setCurrentInnerShapeType(int st) {
		currentInnerShapeType = ShapeType.getShapeType(st);
		System.out.println(currentInnerShapeType);
	}
	public PathType getCurrentPathType() { return currentPathType; }
	public void setCurrentPathType(int pt) {
		currentPathType = PathType.getPathType(pt);
	}
	public void setCurrentHeight(int h) {
		currentHeight = h;
		for (Shape currentShape: root.getAllInnerShapes())
			if ( currentShape.isSelected())
				currentShape.setHeight(currentHeight);
	}
	public void setCurrentWidth(int w) {
		currentWidth = w;
		for (Shape currentShape: root.getAllInnerShapes())
			if ( currentShape.isSelected())
				currentShape.setWidth(currentWidth);
	}
	public void setCurrentColor(Color bc) {
		currentColor = bc;
		for (Shape currentShape: root.getAllInnerShapes())
			if ( currentShape.isSelected())
				currentShape.setColor(currentColor);
	}
	public String getCurrentText() { return currentText; }
	public void setCurrentText(String text) {
		currentText = text;
		for (Shape currentShape: root.getAllInnerShapes())
			if ( currentShape.isSelected())
				currentShape.setText(currentText);
	}
    // you don't need to make any changes after this line ______________
    /**    update the painting area
     * @param g    the graphics control */
    public void update(Graphics g){ paint(g); }
    /** reset the margin size of all shapes from our ArrayList */
    public void resetMarginSize() {
        marginWidth = getWidth();
        marginHeight = getHeight() ;
        for (Shape currentShape: root.getAllInnerShapes())
			currentShape.setMarginSize(marginWidth,marginHeight );
    }
    class MyMouseAdapter extends MouseAdapter {
		public void mouseClicked( MouseEvent e ) {
			boolean found = false;
			for (Shape currentShape: root.getAllInnerShapes())
				if ( currentShape.contains( e.getPoint()) ) { // if the mousepoint is within a shape, then set the shape to be selected/deselected
					currentShape.setSelected( ! currentShape.isSelected() );
					found = true;
				}
			if (!found) createNewShape(e.getX(), e.getY());
		}
	}
	public void start() {
        animationThread = new Thread(this);
        animationThread.start();
    }
    public void stop() {
        if (animationThread != null) {
            animationThread = null;
        }
    }
    public void run() {
        Thread myThread = Thread.currentThread();
        while(animationThread==myThread) {
            repaint();
            pause(DELAY);
        }
    }
    private void pause(int milliseconds) {
        try {
            Thread.sleep((long)milliseconds);
        } catch(InterruptedException ie) {}
    }
}
