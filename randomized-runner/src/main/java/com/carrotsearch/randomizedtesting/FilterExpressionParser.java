package com.carrotsearch.randomizedtesting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test group conditions filter parser. 
 */
public final class FilterExpressionParser {
  static final Node [] EMPTY = new Node [0];

  @SuppressWarnings("serial")
  final class SyntaxException extends RuntimeException {
    final Node node;

    SyntaxException(Node node, String msg) {
      super(msg);
      this.node = node;
    }
    
    @Override
    public String getMessage() {
      if (node != null && node.range != null) {
        return super.getMessage() + " At: \"" + node.range + "\""; 
      } else {
        return super.getMessage();
      }
    }
  }
  
  interface IContext {
    boolean defaultValue();
    boolean hasGroup(String value); 
  }

  static class InputRange {
    final String s;
    final int start;
    final int len;
    
    InputRange(String value, int start, int length) {
      this.s = value;
      this.start = start;
      this.len = length;
    }

    public String value() {
      return s.substring(start, start + len);
    }

    public String toString() {
      return s.substring(0, start) + 
          ">" + value() + 
          "<" + s.substring(start + len);
    }
  }

  public abstract class Node { 
    int lbp;
    Node [] args = EMPTY;
    public InputRange range;

    Node nud() {
      throw new SyntaxException(this, "Syntax error."); 
    }

    Node led(Node left) {
      throw new SyntaxException(this, "Not an operator.");
    }

    @Override
    public String toString() {
      return getClass().getSimpleName().replace("Node", "");
    }

    public final String toExpression() {
      return toExpression(new StringBuilder()).toString();
    }
    
    protected StringBuilder toExpression(StringBuilder b) {
      throw new UnsupportedOperationException("Not an expression node: " + toString());
    }

    public boolean evaluate(IContext context) {
      throw new UnsupportedOperationException("Not an evaluation node: " + toString());
    }
  }

  class EosNode extends Node {
    public EosNode() {
      this.lbp = -1;
    }
  }

  class DefaultNode extends Node {
    @Override
    Node nud() {
      return this;
    }
    
    @Override
    protected StringBuilder toExpression(StringBuilder b) {
      b.append("default");
      return b;
    }
    
    @Override
    public boolean evaluate(IContext context) {
      return context.defaultValue();
    }
  }

  abstract class InfixNode extends Node {
    @Override
    Node led(Node left) {
      if (!nodes.hasNext()) {
        throw new SyntaxException(this, "Missing argument for " 
            + toString().toUpperCase(Locale.ROOT) + ".");
      }

      args = new Node [] {left, expression(lbp)};
      return this;
    }
  }

  class AndNode extends InfixNode {
    public AndNode() {
      this.lbp = 30;
    }
    
    @Override
    protected StringBuilder toExpression(StringBuilder b) {
      assert args.length == 2;
      b.append("(");
      b.append(super.args[0].toExpression());
      b.append(" AND ");
      b.append(super.args[1].toExpression());
      b.append(")");
      return b;
    }
    
    @Override
    public boolean evaluate(IContext context) {
      assert args.length == 2;
      return super.args[0].evaluate(context) &&
             super.args[1].evaluate(context);
    }
  }

  class OrNode extends InfixNode {
    public OrNode() {
      this.lbp = 20;
    }
    
    @Override
    protected StringBuilder toExpression(StringBuilder b) {
      assert args.length == 2;
      b.append("(");
      b.append(super.args[0].toExpression());
      b.append(" OR ");
      b.append(super.args[1].toExpression());
      b.append(")");
      return b;
    }    

    @Override
    public boolean evaluate(IContext context) {
      assert args.length == 2;
      return super.args[0].evaluate(context) ||
             super.args[1].evaluate(context);
    }
  }

  class NotNode extends Node {
    public NotNode() {
      this.lbp = 40;
    }

    @Override
    Node nud() {
      args = new Node[] { expression(lbp) };
      return this;
    }
    
    @Override
    protected StringBuilder toExpression(StringBuilder b) {
      assert args.length == 1;
      b.append("(NOT ");
      b.append(super.args[0].toExpression());
      b.append(")");
      return b;
    }    
    
    @Override
    public boolean evaluate(IContext context) {
      assert args.length == 1;
      return !super.args[0].evaluate(context);
    }
  }

  class OpeningBracketNode extends Node {
    @Override
    Node nud() {
      Node expr = expression(0);

      if (current.getClass() != ClosingBracketNode.class) {
        throw new SyntaxException(current, "Expected closing bracket.");
      }
      current = nodes.next();

      return expr;
    }
  }

  class ClosingBracketNode extends Node {
    @Override
    Node led(Node left) {
      throw new SyntaxException(this, "Unbalanced parenthesis.");
    }
  }

  class TestGroupNode extends Node {
    @Override
    Node nud() {
      return this;
    }
    
    @Override
    protected StringBuilder toExpression(StringBuilder b) {
      b.append(range.value());
      return b;
    }
    
    @Override
    public boolean evaluate(IContext context) {
      return context.hasGroup(range.value());
    }
  }

  private Iterator<Node> nodes;
  private Node current;

  Node expression(int rbp) {
    Node n = current;
    current = nodes.next();
    Node left = n.nud();
    while (rbp < current.lbp) {
      n = current;
      current = nodes.next();
      left = n.led(left);
    }
    return left;
  }

  /**
   * Pratt's parser. 
   */
  public Node parse(String rule) {
    nodes = tokenize(rule);
    current = nodes.next();
    if (current instanceof EosNode) {
      return new DefaultNode();
    } else {
      return expression(-1);
    }
  }

  /**
   * Very simple regexp based tokenizer. We don't need to be fancy or super-fast.
   */
  private Iterator<Node> tokenize(String rule) {
    Matcher m = Pattern.compile(
        "(?:\\s*)(([^\\s\\(\\)]+)|([\\(\\)]))").matcher(rule);
    List<Node> tokens = new ArrayList<Node>();
    while (m.find()) {
      final int s = m.start(1);
      final int len = m.end(1) - s;
      final String value = m.group(1);

      final Node t;
      if (value.equalsIgnoreCase("DEFAULT")) {
        t = new DefaultNode();
      } else if (value.equals("(")) {
        t = new OpeningBracketNode();
      } else if (value.equals(")")) {
        t = new ClosingBracketNode();
      } else if (value.equalsIgnoreCase("and")) {
        t = new AndNode();
      } else if (value.equalsIgnoreCase("or")) {
        t = new OrNode();
      } else if (value.equalsIgnoreCase("not")) {
        t = new NotNode();
      } else if (value.startsWith("@")) {
        t = new TestGroupNode();
      } else {
        throw new SyntaxException(null, String.format(Locale.ROOT,
            "Unrecognized token '%s'. At: \"%s\"",
            value == null ? "<null>" : value,
            new InputRange(rule, s, len)));
      }

      t.range = new InputRange(rule, s, len);
      tokens.add(t);
    }
    
    Node eos = new EosNode();
    eos.range = new InputRange(rule, rule.length(), 0);
    tokens.add(eos);
    return tokens.iterator();
  }
}
