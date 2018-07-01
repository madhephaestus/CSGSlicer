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
	def readers=new HashMap<>()
	ArrayList<Line3D> showEdges(ArrayList<Edge> edges,double offset, javafx.scene.paint.Color color ){
		
		 ArrayList<Line3D> lines =[]
		for(Edge e: edges){
			
			double z=offset
			p1 = new Vector3d(e.getP1().x,e.getP1().y,z)
			p2 = new Vector3d(e.getP2().x,e.getP2().y,z)
			Line3D line = new Line3D(p1,p2);
			line.setStrokeWidth(0.8);
			line.setStroke(color);
			lines .add(line);
			BowlerStudioController.getBowlerStudio() .addNode(line)
		}
		return lines
	}
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
		double size =1024
		
		
		xPix = size*(ratioOrentation?1.0:ratio);
		yPix = size*(!ratioOrentation?1.0:ratio);
		int pixels = (xPix+2)*(yPix+2)
		double xOffset = slicePart.getMinX()
		double yOffset = slicePart.getMinY()
		double scaleX = slicePart.getTotalX()/xPix
		double scaleY = slicePart.getTotalY()/yPix

		println "Image x=" +xPix+" by y="+yPix+" at x="+xOffset+" y="+yOffset
		
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
		ImageView sliceImage = new ImageView(obj_img);
		//sliceImage.getTransforms().add(javafx.scene.transform.Transform.translate(xOffset-imageOffsetMotion, yOffset-imageOffsetMotion));
		//sliceImage.getTransforms().add(javafx.scene.transform.Transform.scale(scaleX,scaleX ));
		BowlerStudioController.getBowlerStudio() .addNode(sliceImage)
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
		def color = pixelReader.getColor((int)absX,(int) absY);

		def value = color.getOpacity()
		//println( ((int)absX)+ " "+((int)absY)+ " "+value+" "+color)
		if(value==0){
			return false
		}
		return true
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
	def SVGCascade(def parts,def slicePart){
		long start = System.currentTimeMillis()
		def obj_img = parts[0]
		def scaleX = parts[1]
		def xOffset=parts[2]
		def scaleY = parts[3]
		def yOffset=parts[4]
		def imageOffset =parts[5]
		def imageOffsetMotion=parts[6]
		double MMTOPX = 3.5409643774783404*100;
		float outputScale = (float) (MMTOPX)
		// Options
		HashMap<String, Float> options = new HashMap<String, Float>();
	        // Tracing
	        options.put("ltres", 1f);// Error treshold for
	                                    // straight lines.
	        options.put("qtres", 1f);// Error treshold for
	                                    // quadratic splines.
	        options.put("pathomit", 0.02f);// Edge node paths
	                                    // shorter than this
	                                    // will be discarded for
	                                    // noise reduction.

	        // Color quantization
	        options.put("colorsampling", 1f); // 1f means true ;
	                                            // 0f means
	                                            // false:
	                                            // starting with
	                                            // generated
	                                            // palette
	        options.put("numberofcolors", 16f);// Number of
	                                            // colors to use
	                                            // on palette if
	                                            // pal object is
	                                            // not defined.
	        options.put("mincolorratio", 0.02f);// Color
	                                            // quantization
	                                            // will
	                                            // randomize a
	                                            // color if
	                                            // fewer pixels
	                                            // than (total
	                                            // pixels*mincolorratio)
	                                            // has it.
	        options.put("colorquantcycles", 1f);// Color
	                                            // quantization
	                                            // will be
	                                            // repeated this
	                                            // many times.
	        //
	        // SVG rendering
	        options.put("scale", outputScale);// Every
	                                            // coordinate
	                                            // will be
	                                            // multiplied
	                                            // with this, to
	                                            // scale the
	                                            // SVG.
	        options.put("simplifytolerance", 1f);//
	        options.put("roundcoords", 2f); // 1f means rounded
	                                        // to 1 decimal
	                                        // places, like 7.3
	                                        // ; 3f means
	                                        // rounded to 3
	                                        // places, like
	                                        // 7.356 ; etc.
	        options.put("lcpr", 0f);// Straight line control
	                                // point radius, if this is
	                                // greater than zero, small
	                                // circles will be drawn in
	                                // the SVG. Do not use this
	                                // for big/complex images.
	        options.put("qcpr",0f);// Quadratic spline control
	                                // point radius, if this is
	                                // greater than zero, small
	                                // circles and lines will be
	                                // drawn in the SVG. Do not
	                                // use this for big/complex
	                                // images.
	        options.put("desc", 0f); // 1f means true ; 0f means
	                                    // false: SVG
	                                    // descriptions
	                                    // deactivated
	        options.put("viewbox", 1f); // 1f means true ; 0f
	                                    // means false: fixed
	                                    // width and height

	        // Selective Gauss Blur
	        options.put("blurradius", 0f); // 0f means
	                                        // deactivated; 1f
	                                        // .. 5f : blur with
	                                        // this radius
	        options.put("blurdelta", 20f); // smaller than this
	                                    // RGB difference
	                                    // will be blurred
		//print "\nTracing..."
		BufferedImage bi = SwingFXUtils.fromFXImage(obj_img,(BufferedImage)null)
		String svg = com.neuronrobotics.bowlerstudio.utils.ImageTracer.imageToSVG(bi,options,(byte[][])null)
		int headerStart = svg.indexOf(">")+1
		int headerEnd = svg.lastIndexOf("<")
		//println "headerStart "+headerStart+ " headerEnd "+headerEnd
		String header = svg.substring(0,headerStart)
		String footer = svg.substring(headerEnd,svg.size())
		String body = svg.substring(headerStart,headerEnd)
		body = "<g id=\"g37\">\n"+body+"</g>\n"
		svg=header+body+footer
		//println header+"\n\n"
		//println body+"\n\n"
		//println footer+"\n\n"
		File tmpsvg = new File( System.getProperty("java.io.tmpdir")+"/"+Math.random())
		tmpsvg.createNewFile()
		FileWriter fw = new FileWriter(tmpsvg.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(svg);
		bw.close();
		double totalScale =scaleX/MMTOPX
		Transform tr = new Transform()
					.translate(xOffset-imageOffsetMotion, yOffset-imageOffsetMotion,0)
					.scale(totalScale)
					//
		SVGLoad l=new SVGLoad(tmpsvg.toURI())	
		l.loadAllGroups(0.0004, 0.0, 0.0);
		ArrayList<Polygon>  svgPolys = l.toPolygons().collect{
			it.transform(tr)
		}
		tmpsvg.delete()
		print "Done Slicing! Took "+((double)(System.currentTimeMillis()-start)/1000.0)+"\n\n"
		
		def okParts = []
		for(int x=0;x<svgPolys.size();x++){
			Polygon tester = svgPolys.get(x)
			Bounds b=tester.getBounds()
			CSG box =  b.toCSG() 
			boolean okToAdd=true
			if(	(slicePart.getTotalX()<(box.getTotalX()-imageOffsetMotion))&&
				(slicePart.getTotalY()<(box.getTotalY()-imageOffsetMotion))
			){
				okToAdd=false
				continue;
			}
			for(Polygon p:okParts){
				Bounds bp=p.getBounds()
				CSG bpBox =bp.toCSG()
				double xdiff = Math.abs(bpBox.getTotalX()-box.getTotalX())
				double ydiff = Math.abs(bpBox.getTotalY()-box.getTotalY())
				double xdiffCenter = Math.abs(box.getCenter().x-bpBox.getCenter().x)
				double ydiffCenter =Math.abs(box.getCenter().y-bpBox.getCenter().y)
				double delta =0.000001
				if(	(xdiff<delta)&&
					(ydiff<delta) &&
					(xdiffCenter<delta)&&
					(ydiffCenter<delta)
				){
					
					okToAdd=false
					//break;
				}
			}
			if(okToAdd){
				okParts.add(svgPolys.get(x))
			}
		}
		println "CSG Sliced to "+okParts.size()+" polygons "
		//println svg
		//BowlerStudioController.getBowlerStudio() .addObject((Object)okParts,(File)null)
		return 	okParts
	}
/**
	 * An interface for slicking CSG objects into lists of points that can be extruded back out
	 * @param incoming			  Incoming CSG to be sliced
	 * @param slicePlane		  Z coordinate of incoming CSG to slice at
	 * @param normalInsetDistance Inset for sliced output
	 * @return					  A set of polygons defining the sliced shape
	 */
	List<Polygon> slice(CSG incoming, Transform slicePlane, double normalInsetDistance){
		List<Polygon> rawPolygons = new ArrayList<>();
		
		// Actual slice plane
		CSG planeCSG = incoming.getBoundingBox()
				.toZMin();
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
		for(def p:rawPolygons){
			for(def v:p.vertices){
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
		showPoints(pixelVersionOfPoints)
		def pixStart = pixelVersionOfPoints.get(0)
		pixelVersionOfPoints.remove(0)
		def nextPoint = pixStart
		def listOfPointsForThisPoly = [pixStart]
		showPoints([nextPoint],30,javafx.scene.paint.Color.ORANGE)
		while((pixelVersionOfPoints.size()>0||listOfPointsForThisPoly.size()>0)&& !Thread.interrupted()){
			nextPoint= searchNext(nextPoint,obj_img)
			showPoints([nextPoint],2,javafx.scene.paint.Color.YELLOW)
			Thread.sleep(1)
			def toRemove = pixelVersionOfPoints.findAll{ withinAPix(nextPoint,it)}
			if(toRemove.size()>0){
					//println "Found "+toRemove
					for(def d:toRemove){
						pixelVersionOfPoints.remove(d)
						listOfPointsForThisPoly.add(d)
					}
					
			}else{
				if(listOfPointsForThisPoly.size()>2){
					if(withinAPix(nextPoint,pixStart)){
						println "Closed Polygon Found!"
						def p =listOfPointsForThisPoly.collect{
							return new Vector3d((it[0]*scaleX)+xOffset,(it[1]*scaleY)+yOffset,0)
						}
						polys.add(Polygon.fromPoints(p))
						
						BowlerStudioController.getBowlerStudio() .addObject(polys, new File("."))
						listOfPointsForThisPoly=[]
						if(pixelVersionOfPoints.size()>0){
							pixStart = pixelVersionOfPoints.remove(0)
							nextPoint = pixStart	
							listOfPointsForThisPoly=[nextPoint]
						}
						showPoints([nextPoint],30,javafx.scene.paint.Color.ORANGE)				
					}
				}
			}
			
		}
		
		def okParts =[]
		readers.clear()
		return okParts
	}
	def searchNext(def pixStart,def obj_img){
	
		def ret = searchNextDepth(pixStart,obj_img,1)
		if (ret==null)
			ret = searchNextDepth(pixStart,obj_img,2)
		return ret
		 
	}
	def searchNextDepth(def pixStart,def obj_img,def searchSize){
		def locations=[]
		for(int i=-searchSize;i<searchSize+1;i++){
			 locations.add([pixStart[0]+searchSize,pixStart[1]+i])
		}
		for(int i=searchSize-1;i>-searchSize-1;i--){
			 locations.add([pixStart[0]+i,pixStart[1]+searchSize])
		}
		for(int i=searchSize-1;i>-searchSize-1;i--){
			 locations.add([pixStart[0]-searchSize,pixStart[1]+i])
		}
		for(int i=-searchSize+1;i<searchSize+1;i++){
			 locations.add([pixStart[0]+i,pixStart[1]-searchSize])
		}
		//println locations
		int searchArraySize=locations.size()

		for(int i=0;i<searchArraySize;i++){
			def counterCW = i-1
			if(counterCW<0)
				counterCW	= searchArraySize-1
			def ccw=locations[counterCW]
			def self=locations[i]
			def w = !pixelBlack(self[0],self[1],obj_img)
			def b = pixelBlack(ccw[0],ccw[1],obj_img)
			if(w&&b){
				// edge detected doing a ccw rotation search
				return self
			}
		}
		
	
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
		
	}
	def withinAPix(def incoming, def out){
		for(int i=-2;i<3;i++){
			int x=incoming[0]+i
			for(int j=-2;j<3;j++){
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
//slices2 = Slice.slice(carrot2.prepForManufacturing(),slicePlane, 0)
//BowlerStudioController.getBowlerStudio().getJfx3dmanager().clearUserNode()
//pin2 = Slice.slice(pin.prepForManufacturing(),slicePlane, 0)
return null
return [carrot,
carrot2,
slices2,
slices]
