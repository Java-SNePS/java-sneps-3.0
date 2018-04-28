/**
 * @className AP.java
 * 
 * @ClassDescription This is the class that acts as an interface to the snepslog 
 *  parser. It contains some static fields and some helper methods used to make 
 *  changes in the backend.
 * 
 * @author Mostafa El-assar
 * @version 3.00 1/4/2018
 */
package sneps.snepslog;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import java_cup.runtime.Symbol;
import sneps.exceptions.CustomException;
import sneps.network.Network;
import sneps.network.Node;
import sneps.network.classes.CaseFrame;
import sneps.network.classes.Relation;
import sneps.network.classes.RelationsRestrictedCaseFrame;
import sneps.network.classes.Semantic;
import sneps.network.classes.Wire;

@SuppressWarnings("deprecation")
public class AP {

	/**
	 * This is a hashtable to store the case frames used in mode 3 where the key is
	 * the name used in creating the case frame.
	 */
	private static Hashtable<String, CaseFrame> modeThreeCaseFrames;

	/**
	 * an integer which holds the number of the snepslog mode currently in use. It
	 * is initially set to 1.
	 */
	private static int snepslogMode = 1;

	/**
	 * a String which holds the name of the printing mode currently in use. It is
	 * initially set to normal.
	 */
	private static String printingMode = "normal";

	/**
	 * @return an int representing the number of the snepslog mode currently in use.
	 */
	protected static int getSnepslogMode() {
		return snepslogMode;
	}

	/**
	 * @param snepslogMode
	 *            the number of the snepslog mode to be used.
	 */
	protected static void setSnepslogMode(int snepslogMode) {
		AP.snepslogMode = snepslogMode;
	}

	/**
	 * @return a String representing the name of the printing mode currently in use.
	 */
	protected static String getPrintingMode() {
		return printingMode;
	}

	/**
	 * @param printingMode
	 *            the name of the printing mode to be used.
	 */
	protected static void setPrintingMode(String printingMode) {
		AP.printingMode = printingMode;
	}

	/**
	 * This method is used to create a customized case frame for mode 1.
	 *
	 * @param noOfArguments
	 *            the number of argument relations.
	 *
	 * @return the case frame after being created.
	 */
	protected static CaseFrame createModeOneCaseFrame(int noOfArguments) {
		LinkedList<Relation> rels = new LinkedList<Relation>();
		Relation r = new Relation("r", "Proposition");
		rels.add(r);
		for (int i = 0; i < noOfArguments; i++) {
			rels.add(new Relation("a" + (i + 1), "Proposition"));
		}
		CaseFrame cf = Network.defineCaseFrame("Proposition", rels);
		return cf;
	}

	/**
	 * This method is used to create a customized case frame for mode 2.
	 * 
	 * @param p
	 *            the name of the p relation.
	 *
	 * @param noOfArguments
	 *            the number of argument relations.
	 *
	 * @return the case frame after being created.
	 */
	protected static CaseFrame createModeTwoCaseFrame(String p, int noOfArguments) {
		LinkedList<Relation> rels = new LinkedList<Relation>();
		Relation r = new Relation("| rel " + p + "|", "Proposition");
		rels.add(r);
		for (int i = 0; i < noOfArguments; i++) {
			rels.add(new Relation("|rel-arg#" + p + (i + 1) + "|", "Proposition"));
		}
		CaseFrame cf = Network.defineCaseFrame("Proposition", rels);
		return cf;
	}

	/**
	 * This method is used to create a case frame for mode 3 and stores it in a
	 * hashtable using the name as key.
	 * 
	 * @param semanticType
	 *            this specifies the semantic type of the case frame.
	 * @param name
	 *            this acts as an identifier for the case frame.
	 * @param relations
	 *            this String contains the relations that is used to create a case
	 *            frame.
	 * @return the case frame after being created.
	 * @throws CustomException
	 *             if a relation was not defined in the Network.
	 */
	protected static CaseFrame createModeThreeCaseFrame(String semanticType, String name, String relations)
			throws CustomException {
		// check if already exists
		if (modeThreeCaseFrames.containsKey(name)) {
			return modeThreeCaseFrames.get(name);
		}
		// remove the brackets
		relations = relations.substring(1, relations.length());
		// divide the relations
		String[] rs = relations.split(" ");
		LinkedList<Relation> rels = new LinkedList<Relation>();
		if (!rs[0].equals("nil")) {
			rels.add(Network.getRelation(rs[0]));
		}
		for (int i = 1; i < rs.length; i++) {
			rels.add(Network.getRelation(rs[i]));
		}
		CaseFrame cf = Network.defineCaseFrame(semanticType, rels);
		modeThreeCaseFrames.put(name, cf);
		return cf;
	}

	/**
	 * This method is used to construct the nodes representing an infixedTerm in the
	 * network.
	 * 
	 * @param type
	 *            a String specifying the type of the infixed term. It should have
	 *            one of the following values: and, or, or equality.
	 * @param arg1
	 *            the first argument node.
	 * @param arg2
	 *            the second argument node.
	 * @return a molecular node representing the infixed term.
	 * @throws Exception
	 */
	protected static Node buildInfixedTerm(String type, Node arg1, Node arg2) throws Exception {
		RelationsRestrictedCaseFrame.createDefaultCaseFrames();
		RelationsRestrictedCaseFrame caseFrame = null;
		ArrayList<Wire> wires = new ArrayList<Wire>();
		switch (type) {
		case "and":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.andOrRule;
			wires.add(new Wire(Relation.max, Network.buildBaseNode("2", new Semantic("Infimum"))));
			wires.add(new Wire(Relation.min, Network.buildBaseNode("2", new Semantic("Infimum"))));
			break;
		case "or":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.andOrRule;
			wires.add(new Wire(Relation.max, Network.buildBaseNode("2", new Semantic("Infimum"))));
			wires.add(new Wire(Relation.min, Network.buildBaseNode("1", new Semantic("Infimum"))));
			break;
		case "equality":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.threshRule;
			wires.add(new Wire(Relation.threshMax, Network.buildBaseNode("1", new Semantic("Infimum"))));
			wires.add(new Wire(Relation.thresh, Network.buildBaseNode("1", new Semantic("Infimum"))));
			break;
		}
		Node infixedTermNode = Network.buildMolecularNode(wires, caseFrame);
		return infixedTermNode;
	}

	/**
	 * This method is used to construct the nodes representing entailments in the
	 * network.
	 * 
	 * @param entailmentType
	 *            a String specifying the type of the entailment. It should have one
	 *            of the following values: AndEntailment, OrEntailment,
	 *            NumericalEntailment or Implication.
	 * @param antecedents
	 *            an ArrayList of the nodes representing the antecedents.
	 * @param consequents
	 *            an ArrayList of the nodes representing the consequents.
	 * @param optionalI
	 *            a String which contains the value of "i" in case of a numerical
	 *            entailment.
	 * @return a molecular node representing the entailment
	 * @throws Exception
	 */
	protected static Node buildEntailment(String entailmentType, ArrayList<Node> antecedents,
			ArrayList<Node> consequents, String optionalI) throws Exception {
		RelationsRestrictedCaseFrame.createDefaultCaseFrames();
		RelationsRestrictedCaseFrame caseFrame = null;
		ArrayList<Wire> wires = new ArrayList<Wire>();
		switch (entailmentType) {
		case "AndEntailment":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.andRule;
			for (int i = 0; i < antecedents.size(); i++) {
				wires.add(new Wire(Relation.andAnt, antecedents.get(i)));
			}
			for (int j = 0; j < consequents.size(); j++) {
				wires.add(new Wire(Relation.cq, consequents.get(j)));
			}
			break;
		case "OrEntailment":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.orRule;
			for (int i = 0; i < antecedents.size(); i++) {
				wires.add(new Wire(Relation.ant, antecedents.get(i)));
			}
			for (int j = 0; j < consequents.size(); j++) {
				wires.add(new Wire(Relation.cq, consequents.get(j)));
			}
			break;
		case "NumericalEntailment":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.numericalRule;
			for (int i = 0; i < antecedents.size(); i++) {
				wires.add(new Wire(Relation.andAnt, antecedents.get(i)));
			}
			for (int j = 0; j < consequents.size(); j++) {
				wires.add(new Wire(Relation.cq, consequents.get(j)));
			}
			wires.add(new Wire(Relation.i, Network.buildBaseNode(optionalI, new Semantic("Infimum"))));
			break;
		case "Implication":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.orRule;
			for (int i = 0; i < antecedents.size(); i++) {
				wires.add(new Wire(Relation.ant, antecedents.get(i)));
			}
			for (int j = 0; j < consequents.size(); j++) {
				wires.add(new Wire(Relation.cq, consequents.get(j)));
			}
			break;
		}
		Node entailmentNode = Network.buildMolecularNode(wires, caseFrame);
		return entailmentNode;
	}

	/**
	 * This method is used to construct the nodes representing a negatedTerm in the
	 * network.
	 * 
	 * @param node
	 *            a node to be negated.
	 * @return a molecular node representing a negatedTerm.
	 * @throws Exception
	 */
	protected static Node buildNegatedTerm(Node node) throws Exception {
		RelationsRestrictedCaseFrame.createDefaultCaseFrames();
		ArrayList<Wire> wires = new ArrayList<Wire>();
		wires.add(new Wire(Relation.arg, node));
		wires.add(new Wire(Relation.max, Network.buildBaseNode("0", new Semantic("Infimum"))));
		wires.add(new Wire(Relation.min, Network.buildBaseNode("0", new Semantic("Infimum"))));
		RelationsRestrictedCaseFrame caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.andOrRule;
		Node negatedNode = Network.buildMolecularNode(wires, caseFrame);
		return negatedNode;
	}

	/**
	 * This method is used to construct the nodes representing an andTerm in the
	 * network.
	 * 
	 * @param i
	 *            the andor min.
	 * @param j
	 *            the andor max.
	 * @param arguments
	 *            an ArrayList of the nodes representing the arguments.
	 * @return a molecular node representing an andorTerm.
	 * @throws Exception
	 */
	protected static Node buildAndorTerm(String i, String j, ArrayList<Node> arguments) throws Exception {
		// TODO andor i j checks
		RelationsRestrictedCaseFrame.createDefaultCaseFrames();
		ArrayList<Wire> wires = new ArrayList<Wire>();
		for (int a = 0; a < arguments.size(); a++) {
			wires.add(new Wire(Relation.arg, arguments.get(a)));
		}
		wires.add(new Wire(Relation.max, Network.buildBaseNode(j, new Semantic("Infimum"))));
		wires.add(new Wire(Relation.min, Network.buildBaseNode(i, new Semantic("Infimum"))));
		RelationsRestrictedCaseFrame caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.andOrRule;
		Node andorNode = Network.buildMolecularNode(wires, caseFrame);
		return andorNode;
	}

	/**
	 * This method is used to construct the nodes representing setTerms in the
	 * network.
	 * 
	 * @param type
	 *            a String specifying the type of the setTerm. It should have one of
	 *            the following values: and, or, nand, nor, xor or iff.
	 * @param arguments
	 *            an ArrayList of the nodes representing the arguments.
	 * @return a molecular node representing a setTerm
	 * @throws Exception
	 */
	protected static Node buildSetTerm(String type, ArrayList<Node> arguments) throws Exception {
		RelationsRestrictedCaseFrame.createDefaultCaseFrames();
		RelationsRestrictedCaseFrame caseFrame = null;
		ArrayList<Wire> wires = new ArrayList<Wire>();
		for (int i = 0; i < arguments.size(); i++) {
			wires.add(new Wire(Relation.arg, arguments.get(i)));
		}
		switch (type) {
		case "and":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.andOrRule;
			wires.add(new Wire(Relation.max, Network.buildBaseNode(arguments.size() + "", new Semantic("Infimum"))));
			wires.add(new Wire(Relation.min, Network.buildBaseNode(arguments.size() + "", new Semantic("Infimum"))));
			break;
		case "or":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.andOrRule;
			wires.add(new Wire(Relation.max, Network.buildBaseNode(arguments.size() + "", new Semantic("Infimum"))));
			wires.add(new Wire(Relation.min, Network.buildBaseNode("1", new Semantic("Infimum"))));
			break;
		case "nand":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.andOrRule;
			wires.add(
					new Wire(Relation.max, Network.buildBaseNode(arguments.size() - 1 + "", new Semantic("Infimum"))));
			wires.add(new Wire(Relation.min, Network.buildBaseNode("0", new Semantic("Infimum"))));
			break;
		case "nor":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.andOrRule;
			wires.add(new Wire(Relation.max, Network.buildBaseNode("0", new Semantic("Infimum"))));
			wires.add(new Wire(Relation.min, Network.buildBaseNode("0", new Semantic("Infimum"))));
			break;
		case "xor":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.andOrRule;
			wires.add(new Wire(Relation.max, Network.buildBaseNode("1", new Semantic("Infimum"))));
			wires.add(new Wire(Relation.min, Network.buildBaseNode("1", new Semantic("Infimum"))));
			break;
		case "iff":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.threshRule;
			wires.add(new Wire(Relation.threshMax,
					Network.buildBaseNode(arguments.size() - 1 + "", new Semantic("Infimum"))));
			wires.add(new Wire(Relation.thresh, Network.buildBaseNode("1", new Semantic("Infimum"))));
			break;
		}
		Node setTermNode = Network.buildMolecularNode(wires, caseFrame);
		return setTermNode;
	}

	/**
	 * This method is used to construct the nodes representing a threshTerm in the
	 * network.
	 * 
	 * @param thresh
	 *            the thresh min.
	 * @param threshmax
	 *            the thresh max.
	 * @param arguments
	 *            an ArrayList of the nodes representing the arguments.
	 * @return a molecular node representing a threshTerm.
	 * @throws Exception
	 */
	protected static Node buildThreshTerm(String thresh, String threshmax, ArrayList<Node> arguments) throws Exception {
		// TODO thresh i j checks
		RelationsRestrictedCaseFrame.createDefaultCaseFrames();
		ArrayList<Wire> wires = new ArrayList<Wire>();
		for (int a = 0; a < arguments.size(); a++) {
			wires.add(new Wire(Relation.arg, arguments.get(a)));
		}
		if (threshmax != null) {
			wires.add(new Wire(Relation.max, Network.buildBaseNode(threshmax, new Semantic("Infimum"))));
		}
		wires.add(new Wire(Relation.min, Network.buildBaseNode(thresh, new Semantic("Infimum"))));
		RelationsRestrictedCaseFrame caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.threshRule;
		Node threshNode = Network.buildMolecularNode(wires, caseFrame);
		return threshNode;
	}

	/**
	 * This method is used to construct the nodes representing a SNeRE TERM in the
	 * network.
	 * 
	 * @param type
	 *            a String specifying the type of the SNeRE term. It should have one
	 *            of the following values: ifdo, whendo, wheneverdo, ActPlan,
	 *            Effect, GoalPlan or Precondition.
	 * @param arg1
	 *            the first argument node.
	 * @param arg2
	 *            the second argument node.
	 * @return a molecular node representing the SNeRE term.
	 * @throws Exception
	 */
	protected static Node buildSNeRETerm(String type, Node arg1, Node arg2) throws Exception {
		RelationsRestrictedCaseFrame.createDefaultCaseFrames();
		RelationsRestrictedCaseFrame caseFrame = null;
		ArrayList<Wire> wires = new ArrayList<Wire>();
		switch (type) {
		case "ifdo":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.doIf;
			wires.add(new Wire(Relation.iff, arg1));
			wires.add(new Wire(Relation.doo, arg2));
			break;
		case "whendo":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.whenDo;
			wires.add(new Wire(Relation.when, arg1));
			wires.add(new Wire(Relation.doo, arg2));
			break;
		case "wheneverdo":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.wheneverDo;
			wires.add(new Wire(Relation.whenever, arg1));
			wires.add(new Wire(Relation.doo, arg2));
			break;
		case "ActPlan":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.planAct;
			wires.add(new Wire(Relation.act, arg1));
			wires.add(new Wire(Relation.plan, arg2));
			break;
		case "Effect":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.actEffect;
			wires.add(new Wire(Relation.act, arg1));
			wires.add(new Wire(Relation.effect, arg2));
			break;
		case "GoalPlan":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.planGoal;
			wires.add(new Wire(Relation.goal, arg1));
			wires.add(new Wire(Relation.plan, arg2));
			break;
		case "Precondition":
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.preconditionAct;
			wires.add(new Wire(Relation.act, arg1));
			wires.add(new Wire(Relation.precondition, arg2));
			break;
		}
		Node snereTerm = Network.buildMolecularNode(wires, caseFrame);
		return snereTerm;
	}

	/**
	 * This method is used to construct the act nodes in the network.
	 * 
	 * @param action
	 *            this is the action node.
	 * @param objects
	 *            this is an arraylist of an arraylist of nodes contains the object
	 *            nodes.
	 * @return a molecular node representing an act node.
	 * @throws Exception
	 */
	protected static Node buildAct(Node action, ArrayList<ArrayList<Node>> objects) throws Exception {
		RelationsRestrictedCaseFrame.createDefaultCaseFrames();
		RelationsRestrictedCaseFrame caseFrame = null;
		ArrayList<Wire> wires = new ArrayList<Wire>();
		ArrayList<Node> objs = new ArrayList<Node>();
		for (int i = 0; i < objects.size(); i++) {
			for (int j = 0; j < objects.get(i).size(); j++) {
				objs.add(objects.get(i).get(j));
			}
		}
		switch (objs.size()) {
		case 1:
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.act;
			wires.add(new Wire(Relation.action, action));
			wires.add(new Wire(Relation.obj, objs.get(0)));
			break;
		case 2:
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.act1;
			wires.add(new Wire(Relation.action, action));
			wires.add(new Wire(Relation.obj1, objs.get(0)));
			wires.add(new Wire(Relation.obj2, objs.get(1)));
			break;
		case 3:
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.act2;
			wires.add(new Wire(Relation.action, action));
			wires.add(new Wire(Relation.obj1, objs.get(0)));
			wires.add(new Wire(Relation.obj2, objs.get(1)));
			wires.add(new Wire(Relation.obj3, objs.get(2)));
			break;
		case 4:
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.act3;
			wires.add(new Wire(Relation.action, action));
			wires.add(new Wire(Relation.obj1, objs.get(0)));
			wires.add(new Wire(Relation.obj2, objs.get(1)));
			wires.add(new Wire(Relation.obj3, objs.get(2)));
			wires.add(new Wire(Relation.obj4, objs.get(3)));
			break;
		case 5:
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.act4;
			wires.add(new Wire(Relation.action, action));
			wires.add(new Wire(Relation.obj1, objs.get(0)));
			wires.add(new Wire(Relation.obj2, objs.get(1)));
			wires.add(new Wire(Relation.obj3, objs.get(2)));
			wires.add(new Wire(Relation.obj4, objs.get(3)));
			wires.add(new Wire(Relation.obj5, objs.get(4)));
			break;
		case 6:
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.act5;
			wires.add(new Wire(Relation.action, action));
			wires.add(new Wire(Relation.obj1, objs.get(0)));
			wires.add(new Wire(Relation.obj2, objs.get(1)));
			wires.add(new Wire(Relation.obj3, objs.get(2)));
			wires.add(new Wire(Relation.obj4, objs.get(3)));
			wires.add(new Wire(Relation.obj5, objs.get(4)));
			wires.add(new Wire(Relation.obj6, objs.get(5)));
			break;
		case 7:
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.act6;
			wires.add(new Wire(Relation.action, action));
			wires.add(new Wire(Relation.obj1, objs.get(0)));
			wires.add(new Wire(Relation.obj2, objs.get(1)));
			wires.add(new Wire(Relation.obj3, objs.get(2)));
			wires.add(new Wire(Relation.obj4, objs.get(3)));
			wires.add(new Wire(Relation.obj5, objs.get(4)));
			wires.add(new Wire(Relation.obj6, objs.get(5)));
			wires.add(new Wire(Relation.obj7, objs.get(6)));
			break;
		case 8:
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.act7;
			wires.add(new Wire(Relation.action, action));
			wires.add(new Wire(Relation.obj1, objs.get(0)));
			wires.add(new Wire(Relation.obj2, objs.get(1)));
			wires.add(new Wire(Relation.obj3, objs.get(2)));
			wires.add(new Wire(Relation.obj4, objs.get(3)));
			wires.add(new Wire(Relation.obj5, objs.get(4)));
			wires.add(new Wire(Relation.obj6, objs.get(5)));
			wires.add(new Wire(Relation.obj7, objs.get(6)));
			wires.add(new Wire(Relation.obj8, objs.get(7)));
			break;
		case 9:
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.act8;
			wires.add(new Wire(Relation.action, action));
			wires.add(new Wire(Relation.obj1, objs.get(0)));
			wires.add(new Wire(Relation.obj2, objs.get(1)));
			wires.add(new Wire(Relation.obj3, objs.get(2)));
			wires.add(new Wire(Relation.obj4, objs.get(3)));
			wires.add(new Wire(Relation.obj5, objs.get(4)));
			wires.add(new Wire(Relation.obj6, objs.get(5)));
			wires.add(new Wire(Relation.obj7, objs.get(6)));
			wires.add(new Wire(Relation.obj8, objs.get(7)));
			wires.add(new Wire(Relation.obj9, objs.get(8)));
			break;
		case 10:
			caseFrame = (RelationsRestrictedCaseFrame) RelationsRestrictedCaseFrame.act9;
			wires.add(new Wire(Relation.obj1, objs.get(0)));
			wires.add(new Wire(Relation.obj2, objs.get(1)));
			wires.add(new Wire(Relation.obj3, objs.get(2)));
			wires.add(new Wire(Relation.obj4, objs.get(3)));
			wires.add(new Wire(Relation.obj5, objs.get(4)));
			wires.add(new Wire(Relation.obj6, objs.get(5)));
			wires.add(new Wire(Relation.obj7, objs.get(6)));
			wires.add(new Wire(Relation.obj8, objs.get(7)));
			wires.add(new Wire(Relation.obj9, objs.get(8)));
			wires.add(new Wire(Relation.obj10, objs.get(9)));
			break;
		}
		Node actNode = Network.buildMolecularNode(wires, caseFrame);
		return actNode;
	}

	/**
	 * This method is used to clear the knowledge base entirely.
	 */
	protected static void clearKnowledgeBase() {
		// TODO Finish building clearKnowledgeBase()
	}

	/**
	 * This method is used to execute a snepslog command.
	 * 
	 * @param command
	 *            a String holding the command that is to be executed.
	 * 
	 * @return a String representing the output of that command.
	 * 
	 * @throws Exception
	 *             if the command is syntactically incorrect.
	 */
	public static String executeSnepslogCommand(String command) throws Exception {
		InputStream is = new ByteArrayInputStream(command.getBytes(StandardCharsets.UTF_8));
		DataInputStream dis = new DataInputStream(is);
		parser parser = new parser(new Lexer(dis));
		parser.command = command;
		Symbol res = parser.parse();
		String output = (String) res.value;
		is.close();
		dis.close();
		return output;
	}

}