package various;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import javax.swing.*;

public class Graph2D extends JFrame{
/**
 * 
 */
int winX, winY, cenX, cenY, offX=0, offY=0;
double zoom, quality;
String equation;
Pattern pattern;
Matcher matcher;
LinkedList<Point> points = new LinkedList<Point>();
JPanel panel, UI;
JButton up,down,left,right,zoomIn,zoomOut;
Timer refresh;
Color textColor,backColor;
JTextField input;
JButton update;
JLabel invalidEquation;
HashMap<String,Integer> ops;
ArrayList<Function> functions;
private static final long serialVersionUID = 1L;
private Scanner scn;
public static void main(String[] args) {
	SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            new Graph2D(100,0.1, Color.WHITE).setVisible(true);//zoom, quality, equation, color
        }
    });
}
//TO-DO: all try catch errors, log base n: do "log[0-9]+(", integrals, sums, impl calculator, multi functions impl invalid equation
public Graph2D(int z,double q, Color c){
	addUI();
	operators();
	System.out.println("Equations graphed:");
	equation="0";
	zoom=z;
	quality=q;
	backColor=c;
	setTitle("Graphing Calculator");
	//setIcon();
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(25, 100, 1000, 1000);
	getContentPane().setSize(5000,5000);
	setLayout(null);
    getContentPane().setBackground(backColor);
    //inverse of screen color
    textColor = new Color(255-backColor.getRed(),255-backColor.getGreen(),255-backColor.getBlue());
    setVisible(true);
	winX=this.getContentPane().getBounds().width;winY=this.getContentPane().getBounds().height;
	cenX=winX/2; cenY=winY/2;
	points= plot(equation);

	panel = new JPanel() {
	private static final long serialVersionUID = 1L;
@Override
public void paint(Graphics g) {//update and draw graphing area
	winX=getContentPane().getBounds().width;winY=getContentPane().getBounds().height;
	panel.setSize(winX,winY);
	cenX=(winX+offX)/2; cenY=(winY+offY)/2;
	super.paint(g);
	Graphics2D g2 = (Graphics2D) g;
	g2.setColor(textColor);
	g2.setStroke(new BasicStroke(1));
	points.clear();
	removeAll();
	points= plot(equation);
	Point prepoint = null;
	//graph function
	for(Point p:points) {
		if (prepoint!=null) {
			g2.drawLine((int)(cenX+zoom*prepoint.x),(int)(cenY-zoom*prepoint.y),(int)(cenX+zoom*p.x),(int)(cenY-zoom*p.y));
		}
		prepoint=p;
	}
	//~10 number labels for x axis
	for(double x=-(cenX/zoom)-quality;x<=(winX-cenX)/zoom+quality;x+=quality*10) {
		if(x>quality||x<-quality){
		JLabel xl=new JLabel(String.format("%."+Math.min((int)Math.ceil(zoom/100),10)+"f",x), SwingConstants.LEFT);
		xl.setBounds((int)(cenX+zoom*x)+5,cenY,80,25);
		xl.setForeground(textColor);
		add(xl);
		}
	}
	//~10 number labels for y axis
	for(double y=-(cenY/zoom)-quality;y<=(winY-cenY)/zoom+quality;y+=quality*10) {
		if(y>quality||y<-quality){
		JLabel yl=new JLabel(String.format("%."+Math.min((int)Math.ceil(zoom/100),10)+"f",-y), SwingConstants.LEFT);
		yl.setBounds(cenX+5,(int)(cenY+zoom*y),80,25);
		yl.setForeground(textColor);
		add(yl);
		}
	}
	//label for (0,0)
	JLabel xl=new JLabel(String.valueOf(0), SwingConstants.LEFT);
	xl.setBounds(cenX+5,cenY,50,25);
	xl.setForeground(textColor);
	add(xl);
	//draw axis'
	g2.setStroke(new BasicStroke(2));
	g2.drawLine(0,cenY,winX,cenY);
	g2.drawLine(cenX,0,cenX,winY);
	g2.dispose();
}
	};
	panel.setOpaque(false);
	panel.setLayout(null);
	panel.setBounds(0,0,winX,winY);
	//refresh every half second
	refresh=new Timer(500, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			panel.repaint();
		}
	});	
	refresh.start();
	add(panel);
}

private void operators() {
	ops = new HashMap<String,Integer>();
	ops.put("+",0);
	ops.put("-",0);
	ops.put("/",5);
	ops.put("*",5);
	ops.put("%",5);
	ops.put("^",10);
	ops.put("sin",10);
	ops.put("cos",10);
	ops.put("tan",10);
	ops.put("cot",10);
	ops.put("csc",10);
	ops.put("sec",10);
	ops.put("sqrt",10);
	ops.put("ln",10);
	ops.put("log",10);
	ops.put("abs",10);
	ops.put("der",10);
	ops.put("int",10);
}
private static class Point{//points
final double x; 
final double y;
public Point(double x, double y) {
	this.x = x;
	this.y = y;
}
}
private class Function{//Functions to have multiple functions at once
LinkedList<Point> points = new LinkedList<Point>();
public Function(LinkedList<Point> p) {
	points=p;
}
}
public Double invalidEquation(String err) {
	invalidEquation.setText(err);
	invalidEquation.setVisible(true);
	return 0.0;
}
//test cases: "1 + 2", "1+2", "1+2*3-4", "sin23", "sin(2+3)", "sin2(3+4)" read sin(2)*(3+4)
public String toRPN(String equation){
	//this is to normalize the string and separate tokens (by /s+) to be read by scanner
	equation=equation.replaceAll("\s+", "");
	while(equation.matches(".*((--)|(\\+\\+)|(\\+-)|(-\\+)).*")) {
		equation=equation.replaceAll("--", "+");
		equation=equation.replaceAll("\\+\\+", "+");
		equation=equation.replaceAll("-\\+", "-");
		equation=equation.replaceAll("\\+-", "-");
	}
	int i;
	pattern = Pattern.compile("sin|cos|tan|cot|sec|csc|sqrt|abs|ln|log|der");//to find trig functions, abs, log10, ln and squareroot without brackets
	i=0;
	matcher = pattern.matcher(equation);
	while (matcher.find())
	{
		int e=matcher.start();
		int l; //length of function because there are different lengths. it takes up less space but increases the runtime
		switch(equation.substring(e+i,e+i+2)){
		case "ln":l=2; break;
		case "sq":l=4; break;
		default: l=3;
		}
		if (equation.charAt(e+3+i)!='(') {
			int j=e+l+1+i;
			for (;j<=equation.length();j++) {
				//System.out.println(j+"."+equation.substring(e+l+i,j)+equation.substring(e+l+i,j).matches("[+-]?[0-9]*(\\.[0-9]*)?x?"));
				if (!equation.substring(e+l+i,j).matches("[+-]?[0-9]*(\\.[0-9]*)?x?")) {break;}
			}j--;
			equation=equation.substring(0,e+l+i)+"("+equation.substring(e+l+i,j)+")"+((j<equation.length())?equation.substring(j):"");
			i+=2;
		}
	}
	pattern = Pattern.compile("[x(]|sin|cos|tan|cot|sec|csc|sqrt|abs|ln|log|der");//to find multiplications without *
	matcher = pattern.matcher(equation);
	i=0;
	while (matcher.find())
	{
		int e=matcher.start();
		if (e-1>=0 && (Character.isDigit(equation.charAt(e-1+i))||equation.charAt(e-1+i)==')')) {
		    equation=equation.substring(0,e+i)+"*"+equation.substring(e+i,equation.length());
		    i++;
		}
	}
	pattern = Pattern.compile("-"); //negatives not minus
	matcher = pattern.matcher(equation);
	i=0;
	while (matcher.find())
	{
		int e=matcher.start();
		if (e+i-1>=0 && !(equation.substring(e-1+i,e+i).matches("[0-9]|[)x]"))) {
			equation=equation.substring(0,e+i)+" "+equation.substring(e+i);
			i++;
		}
		else{
			equation=equation.substring(0,e+i)+" "+equation.charAt(e+i)+" "+equation.substring(e+1+i,equation.length());
			i+=2;
		}
	}
	pattern = Pattern.compile("[()+*^/%]"); //separate operators to make tokens
	matcher = pattern.matcher(equation);
	i=0;
	while (matcher.find())
	{
		int e=matcher.start();
		equation=equation.substring(0,e+i)+" "+equation.charAt(e+i)+" "+equation.substring(e+1+i,equation.length());
		i+=2;
	}
	System.out.println(equation);
	//This is the start of toRPN
	String out="";
	scn = new Scanner(equation).useDelimiter("\s+");
	Stack<String> operators = new Stack<String>();
	while(scn.hasNext()) {
		String e=scn.next();
		//System.out.println(e+": "+out);
		if (ops.containsKey(e)) {
			while (!operators.isEmpty() && ops.containsKey(operators.peek())){
				String tOp=operators.peek();
				if((!e.equals("^")&&ops.get(e)<=ops.get(tOp))||(e.equals("^")&&ops.get(e)<ops.get(tOp))) {
					out+=operators.pop()+" ";
					continue;
				}
				break;
			}
			operators.push(e);
		}
		else if (e.equals("(")) {
			operators.push(e);
		}
		else if (e.equals(")")) {
			while (!operators.isEmpty() && !operators.peek().equals("(")){
				out+=operators.pop()+" ";
			}
			operators.pop();
		}
		else {
			out+=e+" ";
		}
	}
	while (!operators.isEmpty()) {
		out+=operators.pop()+" ";
	}
	//System.out.println(out);
	return out;
}
public Double RPNtoNum(String equ, double x) {
	String eq=equ.replaceAll("x", String.valueOf(x)).replaceAll("--", "+");
	scn=new Scanner(eq).useDelimiter("\s+");
	pattern = Pattern.compile("[+*^/%-]");
	Stack<Double> stack=new Stack<Double>();
	while (scn.hasNext()) {
		String e=scn.next();
		if (ops.containsKey(e)) {
			double i;
			switch (e) {
			case "+": i=stack.pop();
				if(stack.isEmpty()) {stack.push(i);}
				else {stack.push(stack.pop()+i);}break;
			case "-": i=stack.pop();
				if(stack.isEmpty()) {stack.push(-i);}
				else {stack.push(stack.pop()-i);}break;
			case "/": i=stack.pop();stack.push(stack.pop()/i);break;
			case "*": i=stack.pop();stack.push(stack.pop()*i);break;
			case "%": i=stack.pop();stack.push(stack.pop()%i);break;
			case "^": i=stack.pop();stack.push(Math.pow(stack.pop(),i));break;
			case "sin": stack.push(Math.sin(stack.pop()));break;
			case "cos": stack.push(Math.cos(stack.pop()));break;
			case "abs": stack.push(Math.abs(stack.pop()));break;
			case "sec": stack.push(Math.pow(Math.cos(stack.pop()),-1));break;
			case "csc": stack.push(Math.pow(Math.sin(stack.pop()),-1));break;
			case "tan": stack.push(Math.tan(stack.pop()));break;
			case "cot": stack.push(Math.pow(Math.tan(stack.pop()),-1));break;
			case "sqrt": stack.push(Math.sqrt(stack.pop()));break;
			case "ln": stack.push(Math.log(stack.pop()));break;
			case "log": stack.push(Math.log10(stack.pop()));break;
			case "der": 
			String equa=equ.substring(0,equ.indexOf("der"));
			stack.push((RPNtoNum(equa,x+quality)-RPNtoNum(equa,x))/quality);
			break;
			default: return invalidEquation("Error: Are you talking gibberish?");
//			case "int":
//				
			}
		}
		else if(e.matches("[+-]?[0-9]*(\\.[0-9]*)?")){
			stack.push(Double.parseDouble(e));
		}
		else {
			return invalidEquation("Error: Are you talking gibberish?");
		}
	}
	invalidEquation.setVisible(false);
	return stack.pop();
}
public LinkedList<Point> plot(String equ) {
LinkedList<Point> out=new LinkedList<Point>();
for(double x=-(cenX/zoom)-quality;x<=(winX-cenX)/zoom+quality;x+=quality) {
	out.add(new Point(x,RPNtoNum(equ,x)));
}
return out;
}

public void addUI() {//ui panel
	UI=new JPanel() {
			private static final long serialVersionUID = 1L;
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		UI.setBounds(0,0,winX,winY);
		update.setBounds(winX-280,0,80,40);
		input.setBounds(winX-200,0,200,40);
		invalidEquation.setBounds(winX-200,40,200,20);
	}
	};
	add(UI);
	UI.setBounds(0,0,3000,3000);
	UI.setLayout(null);
	UI.setOpaque(false);
	update = new JButton("update");update.setBounds(winX-280,0,80,40);UI.add(update);update.setBackground(backColor);update.setForeground(textColor);
	update.addActionListener( new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {equation=toRPN(input.getText());panel.repaint();}
	});
	input = new JTextField();input.setBounds(winX-200,0,200,40);UI.add(input);input.setBackground(backColor);input.setForeground(textColor);
	input.setEditable(true);
	invalidEquation = new JLabel("Invalid Equation",SwingConstants.CENTER);
	invalidEquation.setBounds(winX-200,40,200,20);UI.add(invalidEquation);invalidEquation.setForeground(textColor);invalidEquation.setVisible(false);
	up = new JButton("/\\");up.setBounds(45,40,45,45);UI.add(up);up.setBackground(backColor);up.setForeground(textColor);
	up.addActionListener( new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {offY-=winY/10;panel.repaint();}
	});
	down = new JButton("\\/");down.setBounds(45,130,45,45);UI.add(down);down.setBackground(backColor);down.setForeground(textColor);
	down.addActionListener( new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {offY+=winY/10;panel.repaint();}
	});
	left = new JButton("<");left.setBounds(0,85,45,45);UI.add(left);left.setBackground(backColor);left.setForeground(textColor);
	left.addActionListener( new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {offX-=winX/10;panel.repaint();}
	});
	right = new JButton(">");right.setBounds(90,85,45,45);UI.add(right);right.setBackground(backColor);right.setForeground(textColor);
	right.addActionListener( new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {offX+=winX/10;panel.repaint();}
	});
	zoomIn = new JButton("><");zoomIn.setBounds(0,0,60,40);UI.add(zoomIn);zoomIn.setBackground(backColor);zoomIn.setForeground(textColor);
	zoomIn.addActionListener( new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {zoom*=1.5;quality/=1.5;panel.repaint();}
	});
	zoomOut = new JButton("<>");zoomOut.setBounds(75,0,60,40);UI.add(zoomOut);zoomOut.setBackground(backColor);zoomOut.setForeground(textColor);
	zoomOut.addActionListener( new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {zoom/=1.5;quality*=1.5;if(zoom<=0){zoom=10;quality=5.7665039062500005;}panel.repaint();}
	});
	
}
}