package com.dealercrest.template;

import java.util.List;

import org.json.JSONArray;

/**
 * Renders a template node once for each item in a list.
 *
 * Supports both java.util.List and org.json.JSONArray as the iterable.
 *
 * PERFORMANCE:
 *   Uses Context.saveAndSet()/restore() — no HashMap allocation per iteration.
 */
public class EachNode extends Node {

    private final String var;
    private final String listName;
    private Node template;

    public EachNode(String var, String listName, Node template) {
        this.var      = var;
        this.listName = listName;
        this.template = template;
    }

    @Override
    public Node compile(DirectiveRegistry registry) {
        template = template.compile(registry);
        return this;
    }

    @Override
    public void render(DataModel ctx, StringBuilder out) {

        Object val = ctx.get(listName);
        if (val == null) return;

        if (val instanceof List) {
            List<?> list = (List<?>) val;
            for (int i = 0; i < list.size(); i++) {
                DataModel.SavedValue saved = ctx.saveAndSet(var, list.get(i));
                template.render(ctx, out);
                ctx.restore(saved);
            }
            return;
        }

        if (val instanceof JSONArray) {
            JSONArray arr = (JSONArray) val;
            for (int i = 0; i < arr.length(); i++) {
                DataModel.SavedValue saved = ctx.saveAndSet(var, arr.opt(i));
                template.render(ctx, out);
                ctx.restore(saved);
            }
            return;
        }
    }
}
