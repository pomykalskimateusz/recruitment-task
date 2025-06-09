package pl.pomykalskimateusz.recruitmenttask;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Table;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseCleaner {

    private static final AtomicReference<List<Table<?>>> computedGraph = new AtomicReference<>();

    private static final Set<String> NOT_REMOVABLE = Set.of();

    public static void cleanAllTables(DSLContext dslContext, String schema) {
        tablesInCorrectOrder(dslContext, schema, true).forEach(table -> dslContext.deleteFrom(table).execute());
    }

    public static void cleanAllTables(DSLContext dslContext, String schema, boolean cache) {
        tablesInCorrectOrder(dslContext, schema, cache).forEach(table -> dslContext.deleteFrom(table).execute());
    }

    private static List<Table<?>> tablesInCorrectOrder(DSLContext dslContext, String schema, boolean cache) {
        if(cache) {
            var tables = computedGraph.get();
            if (tables == null) {
                var dependencies = computeDependencyGraph(dslContext, schema);
                computedGraph.set(dependencies);
                return dependencies;
            }
            return tables;
        } else {
            return computeDependencyGraph(dslContext, schema);
        }
    }

    private static List<Table<?>> computeDependencyGraph(DSLContext dslContext, String schema) {
        var graph = new DependencyGraph();

        dslContext.meta().getTables()
                .stream()
                .filter(DatabaseCleaner::isRemovable)
                .filter(it -> it.getSchema() != null)
                .filter(it -> it.getSchema().getName().equalsIgnoreCase(schema))
                .forEach(table -> graph.addNeighborhood(table, fetchDependencies(table)));

        return graph.dependenciesFirst();
    }

    private static List<Table<?>> fetchDependencies(Table<?> table) {
        List<? extends Table<?>> list = table.getReferences()
                .stream()
                .map(it -> it.getKey().getTable())
                .filter(DatabaseCleaner::isRemovable)
                .toList();
        return (List<Table<?>>) list;
    }

    private static boolean isRemovable(Table<?> table) {
        return !NOT_REMOVABLE.contains(table.getName());
    }

    private static class DependencyGraph {
        private final Map<Table<?>, List<Table<?>>> neighborhoods;

        private DependencyGraph() {
            this.neighborhoods = new HashMap<>();
        }

        public void addNeighborhood(Table<?> to, List<Table<?>> neighbors) {
            neighborhoods.computeIfAbsent(to, s -> new ArrayList<>()).addAll(neighbors);
        }

        public List<Table<?>> dependenciesFirst() {
            var stack = new LinkedList<Table<?>>();
            Set<Table<?>> visited = new HashSet<>();
            neighborhoods.forEach((table, value) -> {
                if (!visited.contains(table)) {
                    topologicalSort(table, visited, stack);
                }
            });
            return stack;
        }

        private void topologicalSort(Table<?> parent, Set<Table<?>> visited, LinkedList<Table<?>> stack) {
            visited.add(parent);
            neighborhoods.get(parent).forEach(dependency -> {
                if (!visited.contains(dependency)) {
                    topologicalSort(dependency, visited, stack);
                }
            });
            stack.push(parent);
        }
    }
}
