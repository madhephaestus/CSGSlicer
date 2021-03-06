import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.vecmath.Matrix4d;

import eu.mihosoft.vrl.v3d.ext.org.poly2tri.DelaunayTriangle;
import eu.mihosoft.vrl.v3d.ext.org.poly2tri.PolygonUtil;
import com.neuronrobotics.bowlerstudio.threed.Line3D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import javax.imageio.ImageIO;
import com.neuronrobotics.bowlerstudio.utils.ImageTracer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.neuronrobotics.nrconsole.util.FileSelectionFactory;
import com.neuronrobotics.sdk.common.Log;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;


println "Loading slicer"

ISlice se2 =new ISlice (){
	double sizeinPixelSpace =1500
	def readers=new HashMap<>()
	def pixelData=new HashMap<>()
	def usedPixels=[]
	def display = false
	def maxRes = 2000
	def minRes = 200
	ArrayList<Line3D> showPoints(def edges,def offset=5, def color=javafx.scene.paint.Color.RED ){
		
		 ArrayList<Line3D> lines =[]
		for(def e: edges){
			
			double z=offset
			p1 = new Vector3d(e[0],e[1],z)
			p2 = new Vector3d(e[0],e[1],-offset)
			Line3D line = new Line3D(p1,p2);
			line.setStrokeWidth(0.8);
			line.setStroke(color);
			lines .add(line);
			BowlerStudioController.getBowlerStudio() .addNode(line)
		}
		return lines
	}
	def toPixMap(def slicePart){
		
		//BowlerStudioController.getBowlerStudio() .addObject((Object)slicePart.movez(1),(File)null)
		//BowlerStudioController.getBowlerStudio() .addObject((Object)rawPolygons,(File)null)
		double ratio = slicePart.getTotalY()/slicePart.getTotalX() 
		boolean ratioOrentation = slicePart.getTotalX()>slicePart.getTotalY() 
		if(ratioOrentation )
		 ratio = slicePart.getTotalX()/slicePart.getTotalY()
		ratio=1/ratio 
		//println "ratio is "+ ratio
		LengthParameter printerOffset 			= new LengthParameter("printerOffset",0.5,[1.2,0])
		double scalePixel = 0.25
		double mySize = slicePart.getTotalX()>slicePart.getTotalY()?slicePart.getTotalX():slicePart.getTotalY()
		def polys = slicePart.getPolygons()
		double size =sizeinPixelSpace*(mySize/200)*(polys.size()/300)
		if(size<minRes)
			size=minRes
		if(size>maxRes)
			size = maxRes
		println "Vectorizing "+polys.size()+" polygons at pixel resolution: "+size
		
		xPix = size*(ratioOrentation?1.0:ratio);
		yPix = size*(!ratioOrentation?1.0:ratio);
		int pixels = (xPix+2)*(yPix+2)
		double xOffset = slicePart.getMinX()
		double yOffset = slicePart.getMinY()
		double scaleX = slicePart.getTotalX()/xPix
		double scaleY = slicePart.getTotalY()/yPix

		//println "New Slicer Image x=" +xPix+" by y="+yPix+" at x="+xOffset+" y="+yOffset
		
		double imageOffset =180.0
		double imageOffsetMotion =imageOffset*scaleX/2
		def imgx =(int)(xPix+imageOffset)
		def imgy = (int)(yPix+imageOffset)
		WritableImage obj_img = new WritableImage(imgx,imgy);
		//int snWidth = (int) 4096;
		//int snHeight = (int) 4096;

		MeshView sliceMesh = slicePart.getMesh();
		sliceMesh.getTransforms().add(javafx.scene.transform.Transform.translate(imageOffsetMotion, imageOffsetMotion));
		AnchorPane anchor = new AnchorPane(sliceMesh);
		AnchorPane.setBottomAnchor(sliceMesh, (double) 0);
		AnchorPane.setTopAnchor(sliceMesh, (double) 0);
		AnchorPane.setLeftAnchor(sliceMesh, (double) 0);
		AnchorPane.setRightAnchor(sliceMesh, (double) 0);
		snapshotGroup = new Pane(anchor);
		snapshotGroup.prefHeight((double)(yPix+imageOffset))
		snapshotGroup.prefWidth((double)(xPix+imageOffset))
		snapshotGroup.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));


		SnapshotParameters snapshotParameters = new SnapshotParameters();
		snapshotParameters.setTransform(new Scale(1/scaleX, 1/scaleY));
		snapshotParameters.setDepthBuffer(true);
		snapshotParameters.setFill(Color.TRANSPARENT);
		
		Runnable r =new Runnable() {
			boolean done =false
			@Override
			public void run() {
				snapshotGroup.snapshot(snapshotParameters, obj_img);
				done=true
			}
		}
		Platform.runLater(r)
		while(r.done== false){
			Thread.sleep(10)
		}

		//println "Find boundries "
		if(display){
		ImageView sliceImage = new ImageView(obj_img);
		//sliceImage.getTransforms().add(javafx.scene.transform.Transform.translate(xOffset-imageOffsetMotion, yOffset-imageOffsetMotion));
		//sliceImage.getTransforms().add(javafx.scene.transform.Transform.scale(scaleX,scaleX ));
		BowlerStudioController.getBowlerStudio() .addNode(sliceImage)
		}
		return [obj_img,scaleX,xOffset-imageOffsetMotion,scaleY,yOffset-imageOffsetMotion,imageOffsetMotion,imageOffset]
	}
	def toPixels(def absX, def absY,def xOff, def yOff, def scaleX,def scaleY){
		return[(int)((absX-xOff)/scaleX),(int)((absY-yOff)/scaleY)]
	}
	def pixelBlack(def absX, def absY,def obj_img){
		if(readers.get(obj_img)==null){
			readers.put(obj_img,obj_img.getPixelReader())
		}
		def pixelReader = readers.get(obj_img);
		return pixelReader.getColor((int)absX,(int) absY).getOpacity()!=0;
	}
	def pixelEdge(def absX, def absY,def obj_img){
		for(int i=-1;i<2;i++){
			int x=absX+i
			for(int j=-1;j<2;j++){
				int y=absY+j
				try{
					if(!pixelBlack(x,y,obj_img)){
						return true		
					}
				}catch(Throwable t){
					BowlerStudio.printStackTrace(t)}
			}
		}
		return false
	}
	
/**
	 * An interface for slicking CSG objects into lists of points that can be extruded back out
	 * @param incoming			  Incoming CSG to be sliced
	 * @param slicePlane		  Z coordinate of incoming CSG to slice at
	 * @param normalInsetDistance Inset for sliced output
	 * @return					  A set of polygons defining the sliced shape
	 */
	List<Polygon> slice(CSG incoming, Transform slicePlane, double normalInsetDistance){
		if(Thread.interrupted()){
			return null
		}
		def startTime = System.currentTimeMillis()
		if(display)BowlerStudioController.getBowlerStudio().getJfx3dmanager().clearUserNode()
		List<Polygon> rawPolygons = new ArrayList<>();
		
		// Actual slice plane
		CSG planeCSG = incoming.getBoundingBox()
				.toZMin();
		planeCSG=planeCSG
				.intersect(
					planeCSG
					.toZMax()
					.movez(0.01)
					)
		// Loop over each polygon in the slice of the incoming CSG
		// Add the polygon to the final slice if it lies entirely in the z plane
		//println "Preparing CSG slice"
		CSG slicePart =incoming
				.transformed(slicePlane)
				.intersect(planeCSG)
		for(Polygon p: slicePart						
				.getPolygons()){
			if(Slice.isPolygonAtZero(p)){
				rawPolygons.add(p);
			}
		}
		
		def parts= toPixMap( slicePart)
		def obj_img = parts[0]
		def scaleX = parts[1]
		def xOffset=parts[2]
		def scaleY = parts[3]
		def yOffset=parts[4]
		def imageOffset =parts[5]
		def imageOffsetMotion=parts[6]

		def points = []
		rawPolygons.collect{
			for(def v:it.vertices){
				points.add(v.pos)
				//pixelBlack(v.pos.x,v.pos.y,obj_img,xOffset,yOffset,scaleX,scaleY)
			}
		}
		
		
		def polys=[]
		
		def pixelVersionOfPoints = points.collect{toPixels(it.x,it.y,xOffset,yOffset,scaleX,scaleY)}
								   .findAll{ pixelEdge( it[0],it[1],obj_img)}
		def pixelVersionOfPointsFiltered =[]
		for(def d:pixelVersionOfPoints){
			boolean testIt=false
			for(def x:pixelVersionOfPointsFiltered){
				if(withinAPix(x,d) ){
					testIt=true
				}	
				
			}
			if(!testIt ){
				pixelVersionOfPointsFiltered.add(d)
			}
		}
		pixelVersionOfPoints=pixelVersionOfPointsFiltered
		if(display)showPoints(pixelVersionOfPoints)
		def pixStart = pixelVersionOfPoints.get(0)
		pixelVersionOfPoints.remove(0)
		def nextPoint = pixStart
		def listOfPointsForThisPoly = [pixStart]
		
		
		if(display)showPoints([nextPoint],20,javafx.scene.paint.Color.ORANGE)
		int lastSearchIndex = 0
		while((pixelVersionOfPoints.size()>0||listOfPointsForThisPoly.size()>0)&& !Thread.interrupted()){
			
			def results= searchNext(nextPoint,obj_img,lastSearchIndex)
			//println "Searching "+results
			if(results==null){
				listOfPointsForThisPoly=[]
				if(pixelVersionOfPoints.size()>0){
					pixStart = pixelVersionOfPoints.remove(0)
					nextPoint = pixStart	
					listOfPointsForThisPoly=[nextPoint]
					if(display)showPoints([nextPoint],40,javafx.scene.paint.Color.BLACK)	
				}else
					break;
				continue;
			}
			nextPoint=results[0]
			lastSearchIndex=results[1]
			if(display)showPoints([nextPoint],2,javafx.scene.paint.Color.YELLOW)
			//Thread.sleep(10)
			def toRemove = pixelVersionOfPoints.findAll{ withinAPix(nextPoint,it)}
			if(toRemove.size()>0){
					//println "Found "+toRemove
					for(def d:toRemove){
						if(display)showPoints([d],30,javafx.scene.paint.Color.GREEN)
						pixelVersionOfPoints.remove(d)
						listOfPointsForThisPoly.add(d)
					}
					
			}else{
				if(listOfPointsForThisPoly.size()>2){
					if(withinAPix(nextPoint,pixStart)){
						if(display)println "Closed Polygon Found!"
						//Thread.sleep(1000)
						def p =listOfPointsForThisPoly.collect{
							return new Vector3d((it[0]*scaleX)+xOffset,(it[1]*scaleY)+yOffset,0)
						}
						def polyNew= Polygon.fromPoints(p)
						polys.add(polyNew)
						
						BowlerStudioController.getBowlerStudio() .addObject([polyNew], new File("."))
						listOfPointsForThisPoly=[]
						if(pixelVersionOfPoints.size()>0){
							pixStart = pixelVersionOfPoints.remove(0)
							nextPoint = pixStart	
							listOfPointsForThisPoly=[nextPoint]
						}
						if(display)showPoints([nextPoint],20,javafx.scene.paint.Color.ORANGE)				
					}
				}
			}
			
		}
		if(listOfPointsForThisPoly.size()>0){
			println "Spare Polygon Found!"
			//Thread.sleep(1000)
			def p =listOfPointsForThisPoly.collect{
				return new Vector3d((it[0]*scaleX)+xOffset,(it[1]*scaleY)+yOffset,0)
			}
			polys.add(Polygon.fromPoints(p))
			if(display)BowlerStudioController.getBowlerStudio() .addObject(polys, new File("."))
		}
		
		readers.clear()
		pixelData.clear
	     usedPixels.clear()
	     //if(display)BowlerStudioController.getBowlerStudio().getJfx3dmanager().clearUserNode()
	     BowlerStudioController.getBowlerStudio() .addObject(polys, new File("."))
	     println "Slice took: "+(((double)(System.currentTimeMillis()-startTime  ))/1000.0)+" seconds"
		return polys
	}
	
	def searchNext(def pixStart,def obj_img,def lastSearchIndex){

		double index=1
		def ret = searchNextDepth(pixStart,obj_img,index,lastSearchIndex)
		
		while(ret == null && index<10&& !Thread.interrupted()){
			index+=0.5
			ret = searchNextDepth(pixStart,obj_img,index,0)
		}
		return ret
		 
	}
	
	def searchNextDepth(def pixStart,def obj_img,def searchSize,def lastSearchIndex){
		def locations=[]
		double inc = Math.toDegrees(Math.atan2(1,searchSize))
		if(searchSize>2){
			for (double i=0;i<360+inc;i+=inc){
				int x = Math.round(
						Math.cos(Math.toRadians(i))*searchSize
					)
				int y = Math.round(
						Math.sin(Math.toRadians(i))*searchSize
					)
				locations.add([pixStart[0]+x,pixStart[1]+y])
			}
		}else{
			
			// arrange the pixels in the data array based on a CCW search
			for(int i=-searchSize;i<searchSize+1;i++){
				 locations.add([pixStart[0]+searchSize,pixStart[1]+i])
			}
			// after the firat loop, leave off the first index to avoid duplicates
			for(int i=searchSize-1;i>-searchSize-1;i--){
				 locations.add([pixStart[0]+i,pixStart[1]+searchSize])
			}
			for(int i=searchSize-1;i>-searchSize-1;i--){
				 locations.add([pixStart[0]-searchSize,pixStart[1]+i])
			}
			for(int i=-searchSize+1;i<searchSize+1;i++){
				 locations.add([pixStart[0]+i,pixStart[1]-searchSize])
			}
			
		}
		//println inc+" "+locations
		//if(searchSize>2)println "\t\t "+searchSize
		int searchArraySize=locations.size()
		if(lastSearchIndex>=searchArraySize){
			lastSearchIndex=0
		}
		int end = lastSearchIndex-1
		if (end<0)
			end = searchArraySize-1
		// rotate throught he data looking for  CCW edge
		for(int i=lastSearchIndex;i!=end && !Thread.interrupted();(i=(i+1>=searchArraySize?0:i+1))){
			//println "\t\t "+i+" start = " +lastSearchIndex+" end = "+end+" array size = "+searchArraySize
			def counterCW = i-1
			if(counterCW<0)
				counterCW	= searchArraySize-1
			def ccw=locations[counterCW]
			def self=locations[i]
			def w = !pixelBlack(self[0],self[1],obj_img)
			def b = pixelBlack(ccw[0],ccw[1],obj_img)
			def useMe = usedPixels.findAll{ it[0]==self[0] && it[1]==self[1]}.size()==0
			if(w&&b&&useMe){
				usedPixels.add(self)
				// edge detected doing a ccw rotation search
				return [self,i]
			}else{
				if(display)showPoints([self],1,javafx.scene.paint.Color.WHITE)		
			}
		}
		
		/*
		//println "From "+pixStart
		def x= pixStart[0]
		def y=pixStart[1]
		def ul = pixelBlack(x+1,y+1,obj_img)
		def uc = pixelBlack(x+1,y,obj_img)
		def ur = pixelBlack(x+1,y-1,obj_img)
		def l= pixelBlack(x,y+1,obj_img)
		def r= pixelBlack(x,y-1,obj_img)
		def bl = pixelBlack(x-1,y+1,obj_img)
		def bc = pixelBlack(x-1,y,obj_img)
		def br = pixelBlack(x-1,y-1,obj_img)
		def me = pixelBlack(x,y,obj_img)
		println  "Ul = "+ul+" uc "+uc+" ur "+ur+" \r\nl "+l+" c "+me+" r "+r+"\r\nbl "+bl+ " bc "+bc+" br "+br
		*/
		
	}
	def withinAPix(def incoming, def out){
		int pixSize=2
		for(int i=-pixSize;i<pixSize+1;i++){
			int x=incoming[0]+i
			for(int j=-pixSize;j<pixSize+1;j++){
				int y=incoming[1]+j
				if(x==out[0] && y == out[1]){
					return true
				}
			}
		}
		return false
	}
}
Slice.setSliceEngine(se2)

if(args != null)
 return
BowlerStudioController.getBowlerStudio().getJfx3dmanager().clearUserNode() 
// Create a CSG to slice
CSG pin = new Cylinder(10, 100)
	.toCSG()
CSG cubePin = new Cube(20,20, 100)
	.toCSG()
CSG carrot = new Cylinder(100,  10)
.toCSG()
.difference(
	[new Cylinder(40, 100)
	.toCSG()
	.movex(75)
	,
	pin.movex(60),
	pin.movex(-60),
	cubePin.movey(60),
	cubePin.movey(-60)
	]
	)
	.movex(-200)
	.movey(-100)
	
	//.roty(30)
	//.rotx(30)
CSG carrot2=carrot.rotz(90).scaley(1).scalex(0.1)
.toYMin().toXMin()
Transform slicePlane = new Transform()

//Image ruler = AssetFactory.loadAsset("BowlerStudio-Icon.png");
//ImageView rulerImage = new ImageView(ruler);
slices = Slice.slice(carrot.prepForManufacturing(),slicePlane, 0)
//BowlerStudioController.getBowlerStudio().getJfx3dmanager().clearUserNode()
slices2 = Slice.slice(carrot2.prepForManufacturing(),slicePlane, 0)
//BowlerStudioController.getBowlerStudio().getJfx3dmanager().clearUserNode()
pin2 = Slice.slice(pin.prepForManufacturing(),slicePlane, 0)
return null
return [carrot,
carrot2,
slices2,
slices,pin2,pin]
