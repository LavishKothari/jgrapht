/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Test JSONExporter
 */
public class JSONExporterTest
{

    @Test
    public void testBasic()
        throws UnsupportedEncodingException,
        ExportException
    {
        String expected =
            "{\"creator\":\"JGraphT JSON Exporter\",\"version\":\"1\",\"nodes\":[{\"id\":\"1\"},{\"id\":\"2\"},{\"id\":\"3\"},{\"id\":\"4\"}],\"edges\":[{\"id\":\"1\",\"source\":\"1\",\"target\":\"2\"},{\"id\":\"2\",\"source\":\"2\",\"target\":\"3\"},{\"id\":\"3\",\"source\":\"3\",\"target\":\"4\"},{\"id\":\"4\",\"source\":\"1\",\"target\":\"4\"}]}";

        Graph<Integer,
            DefaultEdge> graph = GraphTypeBuilder
                .directed().edgeClass(DefaultEdge.class)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).allowingMultipleEdges(false)
                .allowingSelfLoops(false).buildGraph();

        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);

        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(1, 4);

        JSONExporter<Integer, DefaultEdge> exporter = new JSONExporter<>();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(graph, os);
        String res = new String(os.toByteArray(), "UTF-8");
        assertEquals(expected, res);
    }

    @Test
    public void testUndirectedWeightedWithWeightsAndColor()
        throws Exception
    {
        String expected =
            "{\"creator\":\"JGraphT JSON Exporter\",\"version\":\"1\",\"nodes\":[{\"id\":\"1\",\"color\":\"yellow\",\"label\":\"V1\"},{\"id\":\"2\",\"color\":\"red\",\"label\":\"V2\"},{\"id\":\"3\",\"label\":\"V3\"}],\"edges\":[{\"id\":\"1\",\"source\":\"1\",\"target\":\"2\",\"color\":\"what?\",\"label\":\"e12\",\"weight\":1.0},{\"id\":\"2\",\"source\":\"1\",\"target\":\"3\",\"color\":\"I have no color!\",\"label\":\"e13\",\"weight\":1.0},{\"id\":\"3\",\"source\":\"2\",\"target\":\"3\",\"color\":\"I have no color!\",\"label\":\"e13\",\"weight\":100.0}]}";

        Graph<Integer,
            DefaultWeightedEdge> graph = GraphTypeBuilder
                .directed().weighted(true).edgeClass(DefaultWeightedEdge.class)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).allowingMultipleEdges(false)
                .allowingSelfLoops(false).buildGraph();

        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);

        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        DefaultWeightedEdge e13 = graph.addEdge(1, 3);
        DefaultWeightedEdge e23 = graph.addEdge(2, 3);

        graph.setEdgeWeight(e23, 100d);

        ComponentAttributeProvider<Integer> vertexAttributeProvider = v -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            switch (v) {
            case 1:
                map.put("color", DefaultAttribute.createAttribute("yellow"));
                map.put("label", DefaultAttribute.createAttribute("V1"));
                break;
            case 2:
                map.put("color", DefaultAttribute.createAttribute("red"));
                map.put("label", DefaultAttribute.createAttribute("V2"));
                break;
            case 3:
                map.put("label", DefaultAttribute.createAttribute("V3"));
                break;
            default:
                break;
            }
            return map;
        };

        ComponentAttributeProvider<DefaultWeightedEdge> edgeAttributeProvider = e -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            if (e.equals(e12)) {
                map.put("color", DefaultAttribute.createAttribute("what?"));
                map.put("label", DefaultAttribute.createAttribute("e12"));
                map.put("weight", DefaultAttribute.createAttribute(graph.getEdgeWeight(e)));
            } else if (e.equals(e13)) {
                map.put("color", DefaultAttribute.createAttribute("I have no color!"));
                map.put("label", DefaultAttribute.createAttribute("e13"));
                map.put("weight", DefaultAttribute.createAttribute(graph.getEdgeWeight(e)));
            } else if (e.equals(e23)) {
                map.put("color", DefaultAttribute.createAttribute("I have no color!"));
                map.put("label", DefaultAttribute.createAttribute("e13"));
                map.put("weight", DefaultAttribute.createAttribute(graph.getEdgeWeight(e)));
            }
            return map;
        };

        JSONExporter<Integer,
            DefaultWeightedEdge> exporter = new JSONExporter<>(
                new IntegerComponentNameProvider<>(), vertexAttributeProvider,
                new IntegerComponentNameProvider<>(), edgeAttributeProvider);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(graph, os);
        String res = new String(os.toByteArray(), "UTF-8");
        assertEquals(expected, res);
    }

    @Test
    public void testAttributeTypes()
        throws Exception
    {
        String expected =
            "{\"creator\":\"JGraphT JSON Exporter\",\"version\":\"1\",\"nodes\":[{\"id\":\"1\",\"stringAttribute\":\"yellow\",\"doubleAttribute\":3.4,\"intAttribute\":3,\"floatAttribute\":3.4,\"longAttribute\":3,\"booleanAttribute\":true},{\"id\":\"2\"}],\"edges\":[{\"id\":\"1\",\"source\":\"1\",\"target\":\"2\",\"color\":\"what?\",\"label\":\"e12\",\"weight\":100.0}]}";
        
        Graph<Integer,
            DefaultWeightedEdge> graph = GraphTypeBuilder
                .directed().weighted(true).edgeClass(DefaultWeightedEdge.class)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).allowingMultipleEdges(false)
                .allowingSelfLoops(false).buildGraph();

        graph.addVertex(1);
        graph.addVertex(2);

        DefaultWeightedEdge e12 = graph.addEdge(1, 2);
        graph.setEdgeWeight(e12, 100d);

        ComponentAttributeProvider<Integer> vertexAttributeProvider = v -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            switch (v) {
            case 1:
                map.put("stringAttribute", DefaultAttribute.createAttribute("yellow"));
                map.put("doubleAttribute", DefaultAttribute.createAttribute(3.4d));
                map.put("intAttribute", DefaultAttribute.createAttribute(3));
                map.put("floatAttribute", DefaultAttribute.createAttribute(3.4f));
                map.put("longAttribute", DefaultAttribute.createAttribute(3l));
                map.put("booleanAttribute", DefaultAttribute.createAttribute(true));
                break;
            default:
                break;
            }
            return map;
        };

        ComponentAttributeProvider<DefaultWeightedEdge> edgeAttributeProvider = e -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            if (e.equals(e12)) {
                map.put("color", DefaultAttribute.createAttribute("what?"));
                map.put("label", DefaultAttribute.createAttribute("e12"));
                map.put("weight", DefaultAttribute.createAttribute(graph.getEdgeWeight(e)));
            }
            return map;
        };

        JSONExporter<Integer,
            DefaultWeightedEdge> exporter = new JSONExporter<>(
                new IntegerComponentNameProvider<>(), vertexAttributeProvider,
                new IntegerComponentNameProvider<>(), edgeAttributeProvider);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(graph, os);
        String res = new String(os.toByteArray(), "UTF-8");
        assertEquals(expected, res);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNotAllowedNanDouble()
        throws Exception
    {
        Graph<Integer,
            DefaultWeightedEdge> graph = GraphTypeBuilder
                .directed().weighted(true).edgeClass(DefaultWeightedEdge.class)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).allowingMultipleEdges(false)
                .allowingSelfLoops(false).buildGraph();

        graph.addVertex(1);

        ComponentAttributeProvider<Integer> vertexAttributeProvider = v -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            switch (v) {
            case 1:
                map.put("NaNAttribute", DefaultAttribute.createAttribute(Double.NaN));
                break;
            default:
                break;
            }
            return map;
        };

        JSONExporter<Integer,
            DefaultWeightedEdge> exporter = new JSONExporter<>(
                new IntegerComponentNameProvider<>(), vertexAttributeProvider,
                new IntegerComponentNameProvider<>(), new EmptyComponentAttributeProvider<>());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(graph, os);
    }

}
