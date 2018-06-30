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
ISlice se = new ISlice (){
		ArrayList<Vertex> uniquePoints = new ArrayList<>();
		ArrayList<ArrayList<Edge>> edges = new ArrayList<>();
		///BowlerStudioController bc = BowlerStudioController.getBowlerStudio() 
	     //BowlerStudioController.clearCSG()
		double COINCIDENCE_TOLERANCE = 0.0001;
		double step =10
		boolean touhing(Vertex point, Edge e){
			return e.contains(point.pos);
		}
		double length(Edge e){
			
			return Math.sqrt(Math.pow(e.getP1().getX()-e.getP2().getX(),2)+
			Math.pow(e.getP1().getY()-e.getP2().getY(),2)+
			Math.pow(e.getP1().getZ()-e.getP2().getZ(),2)
				);
		}
		
		boolean same(Edge point, Edge e){
			if(e.getP1()==point.getP1() && e.getP2()==point.getP2() )
				return true;
			if(e.getP1()==point.getP2() && e.getP2()==point.getP1() )
				return true	;
				
			return false;
		}
		boolean touching(Vertex p1, Vertex p2){
			
			return eq(p1.pos,p2.pos)
		}

		Vertex existing (Vertex desired){
			if(Math.abs(desired.getZ())>COINCIDENCE_TOLERANCE){
				//println "Bad point! "+desired
				throw new RuntimeException("Bad point!");
			}
			for(Vertex existing:uniquePoints)
						if(	touching(desired,existing)){
							return 	existing;		
						}
			return null;
		}
		Vertex getUnique(Vertex desired){
			Vertex exist = existing(desired)
			if(exist!= null){
				return exist
			}
			uniquePoints.add(desired);
			return desired;
		}
		
		boolean triangleMatchList(Polygon tester,List<Polygon> other){
			for(Polygon p:other){
				if(triangleMatch(tester,p)){
					return true
				}
			}
			return false
		}
		boolean triangleMatch(Polygon tester,Polygon other){
			List<Edge> A = Edge.fromPolygon(tester)
			List<Edge> B = Edge.fromPolygon(other)
			for(int i=0;i<3;i++){
				int i0=i
				int i1=i+1
				int i2=i+2
				if(i1>2)
					i1=i1-3
				if(i2>2)
					i2=i2-3	
				//println "Testing trangle orentation "+i0+" "+i1+ " "+i2
				if(	(edgeMatch(A.get(0), B.get(i0)) &&
					edgeMatch(A.get(1), B.get(i1)) )||
					(edgeMatch(A.get(0), B.get(i1)) &&
					edgeMatch(A.get(1), B.get(i0)) )
				){
					//println "Matching tringle found"
					return true
				}
			}
			return false
		}
		ArrayList<Polygon> filterDuplicateTrangles(List<Polygon> incoming){
			ArrayList<Polygon> unique = []
			for(Polygon tester:incoming){
				if(!triangleMatchList(tester,unique)){
					unique.add(tester)
				}
			}
			return unique
		}

		boolean edgeMatch(Edge tester,Edge myEdge){
			
			if((tester!=null) && (myEdge!=null)){
				boolean p1Shared =  eq(myEdge.getP1().pos,tester.getP1().pos)&&
								eq(myEdge.getP2().pos,tester.getP2().pos)
				boolean p2Shared =  eq(myEdge.getP1().pos,tester.getP2().pos)&&
								eq(myEdge.getP2().pos,tester.getP1().pos	)					
				return p1Shared||p2Shared
				
			}
			return false
		}
		boolean sharePoint(Edge tester,Edge myEdge){
			
			if((tester!=null) && (myEdge!=null)){			
				return 	eq(myEdge.getP1().pos,tester.getP1().pos)||
						eq(myEdge.getP1().pos,tester.getP2().pos)||
						eq(myEdge.getP2().pos,tester.getP1().pos)||
						eq(myEdge.getP2().pos,tester.getP2().pos)
			}
			return false
		}
		ArrayList<Edge> uniqueOnly(ArrayList<Edge> newList){
			
			ArrayList<Edge> edgesOnly = []
			ArrayList<Edge> rejected = []
			for(int i=0;i<newList.size();i++){
				Edge myEdge = newList.get(i);
				if(myEdge!=null){
					boolean internalEdge = false;
					for(int j=0;j<rejected.size();j++){
						
							Edge tester=rejected.get(j);
							if(tester!=null){
								if(edgeMatch(tester,myEdge)){
									//println "Already rejected Line "+myEdge+" "+tester
									internalEdge=true;
								}
							}
						
					}
					if(!internalEdge){
						for(int j=0;j<newList.size();j++){
							if(i!=j){
								Edge tester=newList.get(j);
								if(tester!=null){
									if(edgeMatch(tester,myEdge)){
										//println "Internal Line "+myEdge+" "+tester
										internalEdge=true;
									}
								}
							}
						}
					}
					
					if(internalEdge==false){
						if(length(myEdge)>COINCIDENCE_TOLERANCE){
							edgesOnly.add(myEdge)
							//println "Adding edge "	+	myEdge				
						}else{
							//println "Rejecting short edge "	+	myEdge	
							rejected.add(myEdge)
						}
						
					}else{
						//println "Rejecting internal edge "	+	myEdge	
						rejected.add(myEdge)
					}
					
					
				}
			}
			return edgesOnly
		}

		void addEdges(Polygon p,ArrayList<Edge> newList){
			List<Vertex> vertices = p.vertices;
			for(int i=0;i<vertices.size()-1;i++){
				try{
					add(new Edge(getUnique(vertices.get(i) ), getUnique(vertices.get(i+1) )),newList);
				}catch(Exception ex){
					//println "Point Pruned "
				}
			}
			try{
				add(new Edge(getUnique(vertices.get(vertices.size()-1) ), getUnique(vertices.get(0) )),newList);
			}catch(Exception ex){
				//println "Point Pruned "
			}
		}
		void add(Edge n,ArrayList<Edge> newList){
			boolean valid=true
			//for(Edge e:newList){
			//	if(edgeMatch(e,n)){
					//valid=false
			//	}
			//}
			if(valid)
				newList.add(n)
		}
		private List<Polygon>  updateEdges(List<Polygon> rawPolygons){
			edges.clear()
			for(Polygon it: rawPolygons){
				ArrayList<Edge> newList = new ArrayList<>();
				edges.add(newList);
				addEdges(it,newList)
			}

			//edges.forEach{// search the list of all edges
			for (int k = 0; k < edges.size(); k++) {
				//println "Checking list k "+k+" of "+edges.size()
				ArrayList<Edge> itList = edges.get(k);
				for (int l = 0; l < itList.size(); l++) {
					//println "Checking list l "+l+" of "+itList.size()
					//Edge myEdge = itList.get(l);
					for(int i=0;i<edges.size();i++){// for each edge we cheack every other edge
						
						ArrayList<Edge> testerList = edges.get(i);
						if(itList==testerList){
							continue;// skip comparing to itself
						}
						//println "Checking list i "+i+" of "+edges.size()
						if(!fixEdgeIntersectingList(l,itList,testerList)){
							i=edges.size()
							//l=l-1
							//println "Falling out of loop to re-search"
						}
					}//i for loop
					
				}

			}
			
			List<Polygon> fixed =  new ArrayList<>();		
					
			for(ArrayList<Edge> el: edges){
				if(el.size()>2){		
					fixed.add( Edge.toPolygon(		
							Edge.toPoints(el)		
							,Plane.XY_PLANE));		
				}		
			}
			step+=10
			return fixed	
		}
		/**
		 * An interface for slicking CSG objects into lists of points that can be extruded back out
		 * @param incoming			  Incoming CSG to be sliced
		 * @param slicePlane		  Z coordinate of incoming CSG to slice at
		 * @param normalInsetDistance Inset for sliced output
		 * @return					  A set of polygons defining the sliced shape
		 */
		public List<Polygon> slice(CSG incoming, Transform slicePlane, double normalInsetDistance){
			//println "Groovy Slicing engine"
			//BowlerStudioController.clearCSG()
			//bc.getJfx3dmanager().clearUserNode()
			uniquePoints.clear()
			edges .clear()
			List<Polygon> rawPolygons = new ArrayList<>();

			// Actual slice plane
			CSG planeCSG = incoming.getBoundingBox()
					.toZMin();
			// Loop over each polygon in the slice of the incoming CSG
			// Add the polygon to the final slice if it lies entirely in the z plane
			//println "Preparing CSG slice"
			for(Polygon p: incoming
					.transformed(slicePlane)
					.intersect(planeCSG)						
					.getPolygons()){
				if(Slice.isPolygonAtZero(p)){
					rawPolygons.add(p);
				}
			}
			
			println "Fixing edges"
			List<Polygon> fixed = updateEdges( rawPolygons)	
			println "Triangulating"
			List<Polygon> triangles =[]
			ArrayList<Edge> allEdges = []
			for (int i = 0; i < fixed.size(); i++) {
				List<List<Edge>> triangleEdges =[]
				triangleEdgesFromPolygon(fixed.get(i),  triangleEdges,triangles )
				for(List<Edge> newTri:triangleEdges){
					allEdges.addAll(newTri)
				}
			}	
			println "Prune to unique edges only"						
			ArrayList<Edge> finalEdges=uniqueOnly(allEdges)

			//println "New edges = "+finalEdges.size()+" to "+allEdges.size()
			println "form boundary Paths"		
	         List<Polygon>boundaryPaths =  boundaryPaths(finalEdges)
	         //println "Boundary paths = "+boundaryPaths.size()
	         //List<Polygon> parts= Edge.boundaryPathsWithHoles(boundaryPaths);       		
		    //println "Returning "  +parts.size()   
		    showEdges(finalEdges,0,javafx.scene.paint.Color.RED)
		    return boundaryPaths;  		
		}

		boolean eq(eu.mihosoft.vrl.v3d.Vector3d v ,eu.mihosoft.vrl.v3d.Vector3d other){
		        if (Math.abs(v.x - other.x) > COINCIDENCE_TOLERANCE) {
		            return false;
		        }
		        if (Math.abs(v.y - other.y) >COINCIDENCE_TOLERANCE) {
		            return false;
		        }
		        if (Math.abs(v.z - other.z) > COINCIDENCE_TOLERANCE) {
		            return false;
		        }
		        return true;
		}
		
		double angleBetween(Vector3d v,Vector3d vMinusOne,Vector3d next){
			
			double angle1 = 0
			if(vMinusOne!=null)
				angle1 = Math.atan2(vMinusOne.y - v.y,
			                	       vMinusOne.x - v.x)+Math.PI;
			double angle2 = Math.atan2(-v.y + next.y,
			                          -v.x + next.x)+Math.PI;
			
			double val= angle2-angle1;
			if(val<-Math.toRadians(165)){
				//val+=(Math.PI*2)
			}
			//println Math.toDegrees(val)+" = "+Math.toDegrees(angle2)+" - "+Math.toDegrees(angle1)
			return val
		}
		Edge findEdgesWithPoint(Vector3d v,Vector3d vMinusOne,ArrayList<Edge> consumable){
			
			double currentSmalestAngle =Math.PI*2
			ArrayList<Edge> map=[]
			HashMap<Double,Edge> angles=[]
			for(Edge e:consumable){
				if(eq(v,e.getP1().pos) ){
				    map.add(e)
				    
				    angles.put(e,Math.toDegrees(angleBetween(v,vMinusOne,e.getP2().pos)))
  
				}else if(eq(v,e.getP2().pos) ){
				    map.add(e)
				    angles.put(e,Math.toDegrees(angleBetween(v,vMinusOne,e.getP1().pos)))
  
				}
			}
			if(map.size()==0)
				return null
			if(map.size()==1){
				//showEdges(map,25,javafx.scene.paint.Color.GREEN)
				return map.get(0)
			}
			//println "Possible paths "+angles.values()
			double smallest =361
			Edge best = null
			for(Edge e:map){
				double angle = angles.get(e)
				
				if(angle<smallest){
					smallest=angle
					best=e;
				}
					
			}
			map.remove(best)
			//showEdges(map,15,javafx.scene.paint.Color.RED)
			//showEdges([best],25,javafx.scene.paint.Color.GREEN)
			//println "Best = "+angles.get(best)
			//Thread.sleep(1000)
			return best
		}
		Edge search(ArrayList<Edge> consumable, List<eu.mihosoft.vrl.v3d.Vector3d> boundaryPath ){
			eu.mihosoft.vrl.v3d.Vector3d v =boundaryPath.get(boundaryPath.size()-1)
			eu.mihosoft.vrl.v3d.Vector3d vMinusOne=null
			if(boundaryPath.size()>1)
				vMinusOne =boundaryPath.get(boundaryPath.size()-2)
				
			Edge found =  findEdgesWithPoint(v,vMinusOne,consumable);
			if(found != null){
				 if(eq(v,found.getP1().pos) ){
				 	boundaryPath.add(found.getP2().pos)
				 }else{
				 	boundaryPath.add(found.getP1().pos)
				 }
			}
			return found
		}
		/**
	     * Returns a list of all boundary paths.
	     *
	     * @param boundaryEdges boundary edges (all paths must be closed)
	     * @return the list
	     */
	    public  List<Polygon> boundaryPaths(List<Edge> boundaryEdges) {
	    		//javafx.scene.paint.Color color = new javafx.scene.paint.Color(Math.random()*0.5+0.5,Math.random()*0.5+0.5,Math.random()*0.5+0.5,1);
			//showEdges(boundaryEdges,-5,color)
	    		double oldCooinc = COINCIDENCE_TOLERANCE
			COINCIDENCE_TOLERANCE = 0.0001
			// the resulting boundary edge
			List<Polygon> result = new ArrayList<>();
			ArrayList<Edge> consumable = []
			for(Edge e:boundaryEdges){
				consumable.add(e)
			}
			List<eu.mihosoft.vrl.v3d.Vector3d> boundaryPath = new ArrayList<>();
			while(consumable.size()>0){
				Edge next=null;
				if(boundaryPath.size()==0){
					//println "Loading new path"
					double distance = 0
					Vector3d v=null
					for(Edge e:consumable){
						p1dist = Math.sqrt(
							Math.pow(e.getP1().x,2)+
							Math.pow(e.getP1().y,2)
							)
						p2dist = Math.sqrt(
							Math.pow(e.getP1().x,2)+
							Math.pow(e.getP1().y,2)
							)
						if(p1dist>distance){
							distance=p1dist
							v=(e.getP1().pos)
						}
						if(p2dist>distance){
							distance=p1dist
							v=(e.getP2().pos)
						}
					}
					boundaryPath.add(v)
					ArrayList<Edge> map=[]
					for(Edge e:consumable){
						if(eq(v,e.getP1().pos) ){
						    map.add(e)
		  
						}else if(eq(v,e.getP2().pos) ){
						    map.add(e)
		  
						}
					}
					distance = 0
					v=null
					for(Edge e:map){
						p1dist = Math.sqrt(
							Math.pow(e.getP1().x,2)+
							Math.pow(e.getP1().y,2)
							)
						p2dist = Math.sqrt(
							Math.pow(e.getP1().x,2)+
							Math.pow(e.getP1().y,2)
							)
						if(p1dist>distance){
							distance=p1dist
							v=(e.getP1().pos)
							next=e;
						}
						if(p2dist>distance){
							distance=p1dist
							v=(e.getP2().pos)
							next=e;
						}
					}
					boundaryPath.add(v)
					consumable.remove(next)
					//println "Starting vector for polygon = "+next
					//showEdges([next],20,javafx.scene.paint.Color.GREEN)
					//showEdges(map,15,javafx.scene.paint.Color.RED)
					//Thread.sleep(2000)
				}
				else{
					
					next = search(consumable,boundaryPath );
					
					if(next == null){
						double i
						/*
						for( i=COINCIDENCE_TOLERANCE;i<1&& next==null;i+=0.1){
							
							next = search(consumable,boundaryPath ,i);
						}
						*/
						if(next !=null){
							//println "Widening search to "+i+" worked "
						}else{
							//println "search failed "+ boundaryPath.size()
							if(boundaryPath.size()>2){
								//println "Last Polygon, correcting"
								boundaryPath.add(boundaryPath.get(boundaryPath.size()-1))
								result.add(Polygon.fromPoints(boundaryPath));
								//println "Unclosed Polygon, adding "+boundaryPath.size()
								boundaryPath.clear()
							}else{
								//println "search failed "+ boundaryPath.size()
								boundaryPath.clear()
							}
						}
					}
					
					if(next!=null){
						consumable.remove(next)
					}
				}
				// check to see the path closed
				if(boundaryPath.size()>2){
					if(eq(boundaryPath.get(0),boundaryPath.get(boundaryPath.size()-1))){
						Polygon p = Polygon.fromPoints(boundaryPath)
						result.add(p);
						//println "Regular polygon detected and added "+boundaryPath.size()
						boundaryPath.clear()
					}
				}
				//Thread.sleep(1)
			}
			if(boundaryPath.size()>2){
				//println "Last Polygon, correcting"
				boundaryPath.add(boundaryPath.get(boundaryPath.size()-1))
				result.add(Polygon.fromPoints(boundaryPath));
				//println "Last Polygon, adding "+boundaryPath.size()
				boundaryPath.clear()
				
			}
			COINCIDENCE_TOLERANCE=oldCooinc
			return result;
	    }
		
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
		void triangleEdgesFromPolygon(Polygon tester, List<List<Edge>> triangleEdgesFinal, List<Polygon> triangles  ){
			List<Vertex> vertices = tester.vertices
			ArrayList<Edge> thisPolyEdges= []
			ArrayList<Edge> incomingEdges= []
			 List<List<Edge>> triangleEdges = []
			addEdges(tester,incomingEdges)// load the boundary edges
			
			for(Edge e:incomingEdges){
				valid=true
				if(length(e)<COINCIDENCE_TOLERANCE){
					valid=false
				}
				if(valid)
					for(Edge n:thisPolyEdges){
						if(edgeMatch(e,n)){
							valid=false
						}
						
					}
				if(valid)
					thisPolyEdges.add(e)
			}

			
			int depth =0
			//showEdges(thisPolyEdges,0, new javafx.scene.paint.Color(Math.random()*0.5+0.5,Math.random()*0.5+0.5,Math.random()*0.5+0.5,1))
			for(Vertex v:vertices){
				for(Vertex t:vertices){
					if(t!=v){
						Edge testEdge = new Edge(v,t)
						boolean valid = true
						for(Edge e:thisPolyEdges){
							
							if(edgeMatch(testEdge,e) &&valid ){
								valid=false
								//println "Invalid "+testEdge +" same as "+e
								
								//showEdges([testEdge],d, new javafx.scene.paint.Color(Math.random()*0.5+0.5,Math.random()*0.5+0.5,Math.random()*0.5+0.5,1))
								//showEdges([e],d, new javafx.scene.paint.Color(Math.random()*0.5+0.5,Math.random()*0.5+0.5,Math.random()*0.5+0.5,1))
							}
							if(e.getIntersection(testEdge).isPresent() &&
							!sharePoint(testEdge,e)
							&&valid ){
								valid=false
								//println "Invalid "+testEdge +" crosses "+e
								//showEdges([testEdge],d, new javafx.scene.paint.Color(Math.random()*0.5+0.5,Math.random()*0.5+0.5,Math.random()*0.5+0.5,1))
								//showEdges([e],d, new javafx.scene.paint.Color(Math.random()*0.5+0.5,Math.random()*0.5+0.5,Math.random()*0.5+0.5,1))
						
							}
						}
						for(Vertex x:vertices){
							if(!touching(x,t) && !touching(x,v))
								if(testEdge.contains(x.pos) &&valid ){// test to see if the edge is a degenetate trangle
									valid=false
									//println "Invalid "+testEdge +" contains "+x
								}
						}
						if(valid){
							//println "adding Edge "+testEdge
							thisPolyEdges.add(testEdge)
							//depth +=10
							//showEdges([testEdge],depth+20, new javafx.scene.paint.Color(Math.random()*0.5+0.5,Math.random()*0.5+0.5,Math.random()*0.5+0.5,1))
						}
					}
				}
			}
			triangleCount = (int)(vertices.size()-2)
			int edgeTriangleCount = (int)(thisPolyEdges.size()-3)
			//showEdges(thisPolyEdges,10, new javafx.scene.paint.Color(Math.random()*0.5+0.5,Math.random()*0.5+0.5,Math.random()*0.5+0.5,1))
			/*
			if( edgeTriangleCount != triangleCount){
				for(int i=0;i<thisPolyEdges.size();i++){
					showEdges([thisPolyEdges.get(i)],
							-15-(i*3), 
							new javafx.scene.paint.Color(Math.random()*0.5+0.5,Math.random()*0.5+0.5,Math.random()*0.5+0.5,1))
				}
				System.out.println("Edge Computing failed! Computed from verticies="+triangleCount+" got "+edgeTriangleCount)
			}
			*/
			//println "\n\nExpecting to add "+edgeTriangleCount+" triangles"
			//
			for(Edge testEdge:thisPolyEdges){
				
				for(Edge e1:thisPolyEdges){
					if(touching(testEdge.getP2(),e1.getP1())){
						for(Edge e2:thisPolyEdges){
							if(touching(e2.getP1(),e1.getP2())){
								if(touching(testEdge.getP1(),e2.getP2())){
									triangleEdges.add([testEdge,e1,e2])
									
									
								}
							}
							if(touching(e2.getP2(),e1.getP2())){
								if(touching(testEdge.getP1(),e2.getP1())){
									triangleEdges.add([testEdge,e1,e2])
									
									
								}
							}
						}
					}
					if(touching(testEdge.getP2(),e1.getP2())){
						for(Edge e2:thisPolyEdges){
							if(touching(e2.getP2(),e1.getP1())){
								if(touching(testEdge.getP1(),e2.getP1())){
									triangleEdges.add([testEdge,e1,e2])
									
									
								}
							}
							if(touching(e2.getP1(),e1.getP1())){
								if(touching(testEdge.getP1(),e2.getP2())){
									triangleEdges.add([testEdge,e1,e2])
								}
							}
						}
					}
				}
			}
			
			/*
			foundTriangles = triangles.size()-startingSize
			if(foundTriangles!=edgeTriangleCount){
				
				throw new RuntimeException("Triangulation failed! Expected "+edgeTriangleCount+" got "+foundTriangles)
			}
*/
			
			for(List<Edge> newTri:triangleEdges){

				if(add( Edge.toPolygon(	Edge.toPoints(newTri)		
										,Plane.XY_PLANE),triangles)){
											triangleEdgesFinal.add(newTri)
											/*
											showEdges(newTri,
													step, 
													new javafx.scene.paint.Color(	Math.random()*0.5+0.5,
																			Math.random()*0.5+0.5,
																			Math.random()*0.5+0.5,1))
											*/
										}
			}
			return 
		}
		
		void trianglesFromPolygon(Polygon tester, List<Polygon> triangles ){
			int startingSize = triangles.size()
			//println "Trinagulating "+tester.vertices
			
			//println "Trinagulating "+vertices
			 List<List<Edge>> triangleEdges =[]
			triangleEdgesFromPolygon(tester,  triangleEdges,triangles )
			
			return // in the 4 vector case solve it anyliticaly
			/*
			eu.mihosoft.vrl.v3d.ext.org.poly2tri.Polygon p = PolygonUtil.fromCSGPolygon(tester);
			eu.mihosoft.vrl.v3d.ext.org.poly2tri.Poly2Tri.triangulate(p);
			List<DelaunayTriangle> t = p.getTriangles();
			for (DelaunayTriangle d:t){
				Polygon testPoly =d.toPolygon()
				add(testPoly,triangles)
			}
			*/
			
		}
		boolean add(Polygon tester, List<Polygon> triangles){
			List<Vertex> vertices = tester.vertices;
			boolean badPoint = false
			if(triangleMatchList( tester, triangles)){
				//println "duplicate filtered"
				return false
			}
			if(uniquePoints!=null)
				for (Vertex v:vertices) {
					if( existing (v) ==null ){
						//badPoint=true;
						//println "Dumping triangle with bad point "+v
					}
				}
			if(badPoint == false){
				
				triangles.add(tester);
				return true
			}
			return false
		}
		private boolean fixEdgeIntersectingList(int l,ArrayList<Edge> itList, ArrayList<Edge> testerList){
			Edge myEdge = itList.get(l);
			
			for(int j=0;j<testerList.size();j++){
				//Thread.sleep(0,10);// force a sleep so that interruptions can be allowed
				Edge tester=testerList.get(j);
				//println "Checking list j "+j+" of "+testerList.size()

				boolean p1Shared = eq(myEdge.getP1().pos,tester.getP1().pos)||
								eq(myEdge.getP1().pos,tester.getP2().pos)
				boolean p2Shared =eq( myEdge.getP2().pos,tester.getP1().pos)||
										eq(myEdge.getP2().pos,tester.getP2().pos	)					
				boolean sharedEndPoints = 	p1Shared||p2Shared
									
				boolean onP1 = tester.contains(myEdge.getP1().pos)&& !p1Shared
				boolean onP2 = tester.contains(myEdge.getP2().pos)&& !p2Shared
										
				int baseIndex = j	
				if(	onP1&&
						onP2){
					//println "Both on line \n" +myEdge.getP1()	+" "+myEdge.getP2()	
					//sub edge lies entirely on the line
					//make 3 new edges to deal with this
					testerList.remove(tester);
					// check the relative length of points
					// we know the path of tester is 1->2 so we interupt it in that order
					Edge fe=new Edge(tester.getP1(),myEdge.getP1())
					Edge se=new Edge(tester.getP1(),myEdge.getP2())
					double lenghtFirstToFirst = length(fe);
					double lenghtFirstToSecond = length(se);
					if(lenghtFirstToFirst<lenghtFirstToSecond){
						testerList.add(baseIndex++,fe);
						testerList.add(baseIndex++,new Edge(myEdge.getP1(),myEdge.getP2()));
						testerList.add(baseIndex++,new Edge(myEdge.getP1(),tester.getP2()));
						//return fixEdgeIntersectingList(myEdge,testerList)// recourse when edge added
					}else{
						testerList.add(baseIndex++,se);
						testerList.add(baseIndex++,new Edge(myEdge.getP2(),myEdge.getP1()));
						testerList.add(baseIndex++,new Edge(myEdge.getP1(),tester.getP2()));
						//return fixEdgeIntersectingList(myEdge,testerList)// recourse when edge added
					}
					
					
				 }// if both points are on the line
				 else{// maybe one is on the line if both arent
					if(onP1){	// point one is on the line segment but not p2	
						//println "P1 on line " +myEdge.getP1()					
						testerList.remove(tester);
						testerList.add(baseIndex++,new Edge(tester.getP1(),myEdge.getP1()));
						testerList.add(baseIndex++,new Edge(myEdge.getP1(),tester.getP2()));
						//return fixEdgeIntersectingList(myEdge,testerList)// recourse when edge added
				
					}						
					if(onP2){	// point 2 is on the line not point one		
						//println "P2 on line " +myEdge.getP2()									
						testerList.remove(tester);
						testerList.add(baseIndex++,new Edge(tester.getP1(),myEdge.getP2()));
						testerList.add(baseIndex++,new Edge(myEdge.getP2(),tester.getP2()));
						//return fixEdgeIntersectingList(myEdge,testerList)// recourse when edge added
					}
				 }
				 if(!sharedEndPoints)
				 if(!onP1 && !onP2){// if both points from the testing edge are not on the line
				 	def intersectionPoint = tester.getIntersection(myEdge)
				 	if(intersectionPoint.isPresent()){
				 		int otherBase = l
				 		Vertex newVertex = getUnique(new Vertex(intersectionPoint.get(),tester.getP1().normal) )
				 		//println "Edges are crossing at point "+newVertex
						testerList.remove(tester);
						testerList.add(baseIndex++,new Edge(tester.getP1(),newVertex));
						testerList.add(baseIndex++,new Edge(newVertex,tester.getP2()));

						itList.remove(myEdge);
						itList.add(otherBase++,new Edge(myEdge.getP1(),newVertex));
						itList.add(otherBase++,new Edge(newVertex,myEdge.getP2()));
						//myEdge=itList.get(l);
						//j=-1// restart loop
				 		return false
				 	}
				 }
			
			}// j for loop
			
			//println "Edge is not touching this polygon without a common point"
			return true;
		}

	};

ISlice se2 =new ISlice (){

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

		//println "Image x=" +xPix+" by y="+yPix+" at x="+xOffset+" y="+yOffset
		
		double imageOffset =180.0
		double imageOffsetMotion =imageOffset*scaleX/2
		WritableImage obj_img = new WritableImage((int)(xPix+imageOffset), (int)(yPix+imageOffset));
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
		sliceImage.getTransforms().add(javafx.scene.transform.Transform.translate(xOffset-imageOffsetMotion, yOffset-imageOffsetMotion));
		sliceImage.getTransforms().add(javafx.scene.transform.Transform.scale(scaleX,scaleY ));
		BowlerStudioController.getBowlerStudio() .addNode(sliceImage)
		return [obj_img,scaleX,xOffset,scaleY,yOffset,imageOffsetMotion,imageOffset]
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
		long start = System.currentTimeMillis()
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
slices2 = Slice.slice(carrot2.prepForManufacturing(),slicePlane, 0)
pin2 = Slice.slice(pin.prepForManufacturing(),slicePlane, 0)
return null
return [carrot,
carrot2,
slices2,
slices]
