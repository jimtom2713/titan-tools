package com.shift.titantools;

import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.diskstorage.Backend;
import com.thinkaurelius.titan.diskstorage.BackendTransaction;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.Entry;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KeyColumnValueStore;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.RecordIterator;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.StoreTransaction;
import com.thinkaurelius.titan.diskstorage.util.ByteBufferUtil;
import com.thinkaurelius.titan.graphdb.database.BackendMutator;
import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;
import com.thinkaurelius.titan.graphdb.database.idhandling.IDHandler;
import com.thinkaurelius.titan.graphdb.database.idhandling.VariableLong;
import com.thinkaurelius.titan.graphdb.idmanagement.IDManager;
import com.thinkaurelius.titan.graphdb.query.AtomicQuery;
import com.thinkaurelius.titan.graphdb.query.SimpleTitanQuery;
import com.thinkaurelius.titan.graphdb.transaction.InternalTitanTransaction;
import com.thinkaurelius.titan.graphdb.types.manager.TypeManager;
import com.thinkaurelius.titan.graphdb.types.system.SystemKey;
import com.thinkaurelius.titan.graphdb.vertices.InternalTitanVertex;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Wraps a Titan graph and exposes various utility methods
 */
public class TitanGraphTools {
    private StandardTitanGraph graph;
    private Method getIndexKeyMethod;
    private Method getIndexValueMethod;
    private Method getKeyedIndexColumnMethod;
    private Method getIndexColumnMethod;

    private Method deleteIndexEntryMethod;
    private Method addIndexEntryMethod;

    private Method queryForEntriesMethod;
    private Method getTypeFromIDMethod;

    private Field backendField;
    private Field typeManagerField;
    private Field idManagerField;

    public TitanGraphTools(StandardTitanGraph graph) throws RepairException {
        this.graph = graph;
        getPrivateMethods();
    }

    public static TitanGraphTools create(StandardTitanGraph graph) throws RepairException {
        return new TitanGraphTools(graph);
    }

    /**
     * pulls out the private graph methods we need for index repair and testing, and makes them
     * publicly accessible
     *
     * @throws RepairException
     */
    private void getPrivateMethods() throws RepairException {
        try {
            getIndexKeyMethod = graph.getClass().getDeclaredMethod("getIndexKey", Object.class);
            getIndexKeyMethod.setAccessible(true);

            getIndexValueMethod = graph.getClass().getDeclaredMethod("getIndexValue", TitanProperty.class);
            getIndexValueMethod.setAccessible(true);

            getKeyedIndexColumnMethod = graph.getClass().getDeclaredMethod("getKeyedIndexColumn", TitanKey.class);
            getKeyedIndexColumnMethod.setAccessible(true);

            getIndexColumnMethod = graph.getClass().getDeclaredMethod("getIndexColumn", TitanKey.class, long.class);
            getIndexColumnMethod.setAccessible(true);

            deleteIndexEntryMethod = graph.getClass().getDeclaredMethod("deleteIndexEntry", TitanProperty.class, BackendMutator.class);
            deleteIndexEntryMethod.setAccessible(true);

            addIndexEntryMethod = graph.getClass().getDeclaredMethod("addIndexEntry", TitanProperty.class, BackendMutator.class);
            addIndexEntryMethod.setAccessible(true);

            queryForEntriesMethod = graph.getClass().getDeclaredMethod("queryForEntries", AtomicQuery.class, StoreTransaction.class);
            queryForEntriesMethod.setAccessible(true);

            getTypeFromIDMethod = graph.getClass().getDeclaredMethod("getTypeFromID", long.class, InternalTitanTransaction.class);
            getTypeFromIDMethod.setAccessible(true);

            backendField = graph.getClass().getDeclaredField("backend");
            backendField.setAccessible(true);

            typeManagerField = graph.getClass().getDeclaredField("etManager");
            typeManagerField.setAccessible(true);

            idManagerField = graph.getClass().getDeclaredField("idManager");
            idManagerField.setAccessible(true);

        } catch (NoSuchFieldException e) {
            throw new RepairException(e);
        } catch (NoSuchMethodException e) {
            throw new RepairException(e);
        }
    }

    /**
     * Calls the getIndexKey method on the graph object
     *
     * @param att
     * @return
     * @throws com.shift.titantools.RepairException
     */
    public ByteBuffer getIndexKey(Object att) throws RepairException {
        try {
            return (ByteBuffer) getIndexKeyMethod.invoke(graph, att);
        } catch (IllegalAccessException e) {
            throw new RepairException(e);
        } catch (InvocationTargetException e) {
            throw new RepairException(e);
        }
    }

    /**
     * Calls the getIndexValue method on the graph object
     *
     * @param prop
     * @return
     * @throws com.shift.titantools.RepairException
     */
    public ByteBuffer getIndexValue(TitanProperty prop) throws RepairException {
        try {
            return (ByteBuffer) getIndexValueMethod.invoke(graph, prop);
        } catch (IllegalAccessException e) {
            throw new RepairException(e);
        } catch (InvocationTargetException e) {
            throw new RepairException(e);
        }
    }

    /**
     * Calls the getKeyedIndexColumn method on the graph object
     *
     * @param type
     * @return
     * @throws com.shift.titantools.RepairException
     */
    public ByteBuffer getKeyedIndexColumn(TitanKey type) throws RepairException {
        try {
            return (ByteBuffer) getKeyedIndexColumnMethod.invoke(graph, type);
        } catch (IllegalAccessException e) {
            throw new RepairException(e);
        } catch (InvocationTargetException e) {
            throw new RepairException(e);
        }
    }

    /**
     * Calls the getIndexColumn method on the graph object
     *
     * @param type
     * @param propertyID
     * @return
     * @throws RepairException
     */
    public ByteBuffer getIndexColumn(TitanKey type, long propertyID) throws RepairException {
        try {
            return (ByteBuffer) getIndexColumnMethod.invoke(graph, type, propertyID);
        } catch (IllegalAccessException e) {
            throw new RepairException(e);
        } catch (InvocationTargetException e) {
            throw new RepairException(e);
        }
    }

    private void deleteIndexEntry(TitanProperty prop, BackendMutator mutator) throws RepairException {
        try {
            deleteIndexEntryMethod.invoke(graph, prop, mutator);
        } catch (IllegalAccessException e) {
            throw new RepairException(e);
        } catch (InvocationTargetException e) {
            throw new RepairException(e);
        }
    }

    private void addIndexEntry(TitanProperty prop, BackendMutator mutator) throws RepairException {
        try {
            addIndexEntryMethod.invoke(graph, prop, mutator);
        } catch (IllegalAccessException e) {
            throw new RepairException(e);
        } catch (InvocationTargetException e) {
            throw new RepairException(e);
        }

    }

    public List<Entry> queryForEntries(AtomicQuery query, StoreTransaction txh) throws RepairException {
        try {
            return (List<Entry>) queryForEntriesMethod.invoke(graph, query, txh);
        } catch (IllegalAccessException e) {
            throw new RepairException(e);
        } catch (InvocationTargetException e) {
            throw new RepairException(e);
        }
    }

    private final TitanType getTypeFromID(long etid, InternalTitanTransaction tx) throws RepairException {
        try {
            return (TitanType) getTypeFromIDMethod.invoke(graph, etid, tx);
        } catch (IllegalAccessException e) {
            throw new RepairException(e);
        } catch (InvocationTargetException e) {
            throw new RepairException(e);
        }
    }

    /**
     * Returns the backend instance of the wrapped graph
     *
     * @return
     * @throws RepairException
     */
    public Backend getBackend() throws RepairException {
        try {
            return (Backend) backendField.get(graph);
        } catch (IllegalAccessException e) {
            throw new RepairException(e);
        }
    }

    public IDManager getIdManager() throws RepairException {
        try {
            return (IDManager) idManagerField.get(graph);
        } catch (IllegalAccessException e) {
            throw new RepairException(e);
        }
    }

    /**
     * Returns the type manager instance of the wrapped graph
     *
     * @return
     * @throws RepairException
     */
    public TypeManager getTypeManager() throws RepairException {
        try {
            return (TypeManager) typeManagerField.get(graph);
        } catch (IllegalAccessException e) {
            throw new RepairException(e);
        }
    }

    private static byte[] getByteArray(ByteBuffer buffer) {
        int offset = buffer.arrayOffset();
        byte[] bytes = new byte[buffer.remaining() - offset];
        System.arraycopy(buffer.array(), offset, bytes, offset, bytes.length);
        return bytes;
    }

    public TitanKey makeType(String name, Class<?> type, Boolean indexed, Boolean unique) {
        TitanTransaction tx = graph.newTransaction();
        TypeMaker t = tx.makeType().name(name).simple().functional();
        if (unique) t = t.unique();
        if (indexed) t = t.indexed();
        TitanKey k = t.dataType(type).group(TypeGroup.DEFAULT_GROUP).makePropertyKey();
        tx.commit();
        return k;
    }

    /**
     * Repairs the index associated with the given type. Note that this
     * method only takes action if it detects an index entry that points
     * to a vertex that either doesn't exist, or whose value for the given
     * type doesn't match the index value pointing to it. It does not
     * perform an exhaustive examination of all vertices to check that
     * they are properly indexed, run reindexType to reindex every occurrence
     * of a property.
     *
     * When an incorrect index entry is found, the erroneous index entry is
     * deleted. If the vertex still exists, * and still contains that the
     * property, the index is updated with the correct property.
     *
     * @param type: the type to examine
     * @param repair: inconsistencies are repaired if this is set to true
     * @return
     * @throws RepairException
     */
    protected void repairType(TitanType type, boolean repair) throws RepairException {

        if (!type.isPropertyKey()) {
            throw new RepairException("the given type is not a property key");
        }

        //begin graph and store transactions
        InternalTitanTransaction itx = (InternalTitanTransaction) graph.newTransaction();
        StoreTransaction stx = ((BackendTransaction) itx.getTxHandle()).getStoreTransactionHandle();

        //get the key
//        TitanKey titanKey = itx.getPropertyKey(type.getName());
        TitanKey titanKey = (TitanKey) type;
        boolean isSystemKey = titanKey.getName().startsWith("#");

        if (!titanKey.hasIndex()) {
            throw new RepairException("the given key is not an index");
        }

        Backend backend = getBackend();
        KeyColumnValueStore indexStore = backend.getVertexIndexStore();

        int keyCount = 0;
        int deletedVertexCount = 0;
        int repairedPropertyCount = 0;
        try {

            //we need to iterate over all keys in the index
            RecordIterator<ByteBuffer> keys = indexStore.getKeys(stx);
            while (keys.hasNext()) {
                ByteBuffer key = keys.next();
                byte[] keyArray = getByteArray(key);

                List<ByteBuffer> deletions = new ArrayList<ByteBuffer>();
                List<TitanProperty> additions = new ArrayList<TitanProperty>();

                ByteBuffer startCol = VariableLong.positiveByteBuffer(titanKey.getID());
                List<Entry> columns = indexStore.getSlice(
                        key,
                        startCol,
                        ByteBufferUtil.nextBiggerBuffer(startCol),
                        stx
                );
                for (Entry entry : columns) {
                    long eid = VariableLong.readPositive(entry.getValue());
                    TitanVertex v = (TitanVertex) graph.getVertex(eid);
                    if (v == null) {
                        deletions.add(entry.getColumn());
                        System.out.println("deleted vertex found in index");
                        deletedVertexCount++;
                    } else if (!isSystemKey) {
                        //verify that the given property matches
                        Iterator<TitanProperty> properties = v.getProperties(titanKey.getName()).iterator();
                        assert properties.hasNext();
                        TitanProperty property = properties.next();
                        assert !properties.hasNext();
                        Object value = property.getAttribute();
                        ByteBuffer indexKey = getIndexKey(value);
                        byte[] valueArray = getByteArray(indexKey);
                        if (!Arrays.equals(keyArray, valueArray)) {
                            deletions.add(entry.getColumn());
                            additions.add(property);
                            System.out.println("value mismatch found in index");
                            repairedPropertyCount++;
                        }
                    }
                }

                if (repair) {
                    InternalTitanTransaction tx = (InternalTitanTransaction) graph.newTransaction();
                    if (deletions.size() > 0 || additions.size() > 0) {
                        BackendMutator mutator = new BackendMutator(backend, tx.getTxHandle());
                        if (deletions.size() > 0) {
                            indexStore.mutate(key, null, deletions, stx);
                        }
                        for (TitanProperty property: additions) {
                            addIndexEntry(property, mutator);
                        }
                        tx.commit();
                    }
                }
                keyCount++;
                if (keyCount % 1000 == 0)
                    System.out.println(keyCount + " keys inspected");
            }
        } catch (StorageException e) {
            throw new RepairException(e);
        } finally {
            //cleanup
            itx.commit();
        }

        System.out.println("");
        System.out.println("[" + type.getName() + "] " + (repair?"repair":"check") + " completed");
        System.out.println("  > " + keyCount + " keys examined");
        System.out.println("  > " + deletedVertexCount + " references to deleted vertices " + (repair?"removed":"detected"));
        System.out.println("  > " + repairedPropertyCount + " incorrectly indexed vertex properties " + (repair?"repaired":"detected"));
    }

    /**
     * Repairs the index associated with the given type. Note that this
     * method only takes action if it detects an index entry that points
     * to a vertex that either doesn't exist, or whose value for the given
     * type doesn't match the index value pointing to it. It does not
     * perform an exhaustive examination of all vertices to check that
     * they are properly indexed, run reindexType to reindex every occurrence
     * of a property.
     *
     * When an incorrect index entry is found, the erroneous index entry is
     * deleted. If the vertex still exists, * and still contains that the
     * property, the index is updated with the correct property.
     *
     * @param type
     * @throws RepairException
     */
    public void repairType(TitanType type) throws RepairException {
        repairType(type, true);
    }

    /**
     * Repairs the index associated with the given type. Note that this
     * method only takes action if it detects an index entry that points
     * to a vertex that either doesn't exist, or whose value for the given
     * type doesn't match the index value pointing to it. It does not
     * perform an exhaustive examination of all vertices to check that
     * they are properly indexed, run reindexType to reindex every occurrence
     * of a property.
     *
     * When an incorrect index entry is found, the erroneous index entry is
     * deleted. If the vertex still exists, * and still contains that the
     * property, the index is updated with the correct property.
     *
     * @param typeName
     * @throws RepairException
     */
    public void repairType(String typeName) throws RepairException {
        TitanType type = graph.getType(typeName);
        if (type == null) {
            throw new RepairException("the type [" + typeName + "] wasn't found");
        }
        repairType(type);
    }

    /**
     * repairs inconsistencies in the type name -> type index
     *
     * @throws RepairException
     */
    public void repairTypeIndex() throws RepairException {
        repairType(SystemKey.TypeName, true);
    }

    /**
     * Detects problems with the index associated with the given type.
     *
     * @param type
     * @throws RepairException
     */
    public void checkType(TitanType type) throws RepairException {
        repairType(type, false);
    }

    /**
     * Checks for inconsistencies in the type name -> type index
     *
     * @throws RepairException
     */
    public void checkTypeIndex() throws RepairException {
        repairType(SystemKey.TypeName, false);
    }


    /**
     * Detects problems with the index associated with the given type.
     *
     * @param typeName
     * @throws RepairException
     */
    public void checkType(String typeName) throws RepairException {
        TitanType type = graph.getType(typeName);
        if (type == null) {
            throw new RepairException("the type [" + typeName + "] wasn't found");
        }
        checkType(type);
    }

    /**
     * Iterates through all vertices and updates the index with the current values
     *
     * This will not detect incorrect entries in the index
     *
     * @param type
     */
    public void reindexType(TitanType type) throws RepairException {
        if (!type.isPropertyKey()) {
            throw new RepairException("the given type is not a property key");
        }

        //begin graph and store transactions
        InternalTitanTransaction itx = (InternalTitanTransaction) graph.newTransaction();
        StoreTransaction stx = ((BackendTransaction) itx.getTxHandle()).getStoreTransactionHandle();

        //get the key
        TitanKey titanKey = itx.getPropertyKey(type.getName());

        if (!titanKey.hasIndex()) {
            throw new RepairException("the given key is not an index");
        }

        //vertices are stored in the edge store
        Backend backend = getBackend();
        KeyColumnValueStore edgeStore = backend.getEdgeStore();

        int count = 0;
        try {
            //@todo: fix this so the mutator and transaction are instantiated and committed once per fix
            InternalTitanTransaction tx = (InternalTitanTransaction) graph.newTransaction();
            BackendMutator mutator = new BackendMutator(backend, tx.getTxHandle());
            RecordIterator<ByteBuffer> keys = edgeStore.getKeys(stx);
            while (keys.hasNext()) {
                ByteBuffer key = keys.next();
                long eid = IDHandler.getKeyID(key);
                TitanVertex v = tx.getVertex(eid);
                Iterator<TitanProperty> properties = v.getProperties(titanKey.getName()).iterator();
                while (properties.hasNext()) {
                    addIndexEntry(properties.next(), mutator);
                    count++;
                }
                if (count % 1000 == 0)
                    System.out.println(count + " properties reindexed on type: [" + type.getName() + "]");
            }
            tx.commit();
        } catch (StorageException e) {
            throw new RepairException(e);
        }
        itx.commit();

        System.out.println("");
        System.out.println(count + " properties reindexed on type: [" + type.getName() + "]");
    }

    /**
     * Iterates through all vertices and updates the index with the current values
     *
     * This will not detect incorrect entries in the index
     *
     * @param typeName
     */
    public void reindexType(String typeName) throws RepairException {
        TitanType type = graph.getType(typeName);
        if (type == null) {
            throw new RepairException("the type [" + typeName + "] wasn't found");
        }
        reindexType(type);
    }

    /**
     * Works the same as v.getProperty(), but it will not exclude system properties
     *
     * @param v
     * @param key
     * @return
     */
    public static Object getSystemPropertyValue(TitanVertex v, TitanKey key) {
        Iterator<TitanProperty> iter = new SimpleTitanQuery((InternalTitanVertex) v)
                .type(key).includeHidden().propertyIterator();

        if (!iter.hasNext()) return null;
        else {
            Object value = iter.next().getAttribute();
            if (iter.hasNext()) throw new QueryException("Multiple properties of specified type: " + key);
            return value;
        }

    }

    public static TitanProperty getSystemProperty(TitanVertex v, TitanKey key) {
        Iterator<TitanProperty> iter = new SimpleTitanQuery((InternalTitanVertex) v)
                .type(key).includeHidden().propertyIterator();

        if (!iter.hasNext()) return null;
        else {
            TitanProperty value = iter.next();
            if (iter.hasNext()) throw new QueryException("Multiple properties of specified type: " + key);
            return value;
        }
    }

    /**
     * Iterates over all vertices in the graph and removes ones that
     * are partially deleted
     *
     * @param repair
     */
    public void cleanVertices(boolean repair) throws RepairException {
        //begin graph and store transactions
        InternalTitanTransaction itx = (InternalTitanTransaction) graph.newTransaction();
        StoreTransaction stx = ((BackendTransaction) itx.getTxHandle()).getStoreTransactionHandle();

        //vertices are stored in the edge store
        Backend backend = getBackend();
        KeyColumnValueStore edgeStore = backend.getEdgeStore();
        Set<Long> removedIndexEntries = new HashSet<Long>();

        Double minCreated = Double.MAX_VALUE;
        Double maxCreated = Double.MIN_VALUE;

        int keyCount = 0;
        int fixCount = 0;
        try {
            InternalTitanTransaction readOnlyTx = (InternalTitanTransaction) graph.newTransaction();
            RecordIterator<ByteBuffer> keys = edgeStore.getKeys(stx);
            while (keys.hasNext()) {
                ByteBuffer key = keys.next();
                long eid = IDHandler.getKeyID(key);
                TitanVertex v = readOnlyTx.getVertex(eid);

                //don't mess with system stuff
                if (v instanceof TitanKey) continue;
                if (v instanceof TitanLabel) continue;
                if (v instanceof TitanType) continue;

                Object state = getSystemPropertyValue(v, SystemKey.VertexState);

                if (state == null) {
                    if (repair) {
                        InternalTitanTransaction tx = (InternalTitanTransaction) graph.newTransaction();
                        tx.removeVertex(tx.getVertex(v.getID()));
                        tx.commit();
                    }
                    System.out.println("invalid vertex found: v[" + v.getID() + "]");
                    fixCount++;
                } else {
                    //
                    SimpleTitanQuery sq = new SimpleTitanQuery((InternalTitanVertex) v);
                    List<Entry> entries = queryForEntries(sq.clone(), stx);
                    List<ByteBuffer> deletions = new LinkedList<ByteBuffer>();
                    List<Long> idxDeletions = new LinkedList<Long>();
                    for (Entry entry: entries) {
                        ByteBuffer column = entry.getColumn();
                        int pos = column.position();
//                        int dirID = IDHandler.getDirectionID(column.get(column.position()));
                        long etid = IDHandler.readEdgeType(column, getIdManager());
                        TitanType titanType = getTypeFromID(etid, readOnlyTx);
                        if (titanType == null) {
                            column.position(pos);
                            deletions.add(entry.getColumn());
                            idxDeletions.add(etid);
                        }
                    }

                    if (deletions.size() > 0) {
                        if (repair) {
                            InternalTitanTransaction tx = (InternalTitanTransaction) graph.newTransaction();
                            StoreTransaction storeTx = ((BackendTransaction) tx.getTxHandle()).getStoreTransactionHandle();
                            getBackend().getEdgeStore().mutate(IDHandler.getKey(v.getID()), null, deletions, storeTx);
                            stx.commit();
                            tx.commit();
                        }
                        System.out.print("corrupt vertex property found: v[" + v.getID() + "] -> ");
                        for (Long l: idxDeletions) System.out.print(" " + l);
                        System.out.print("\n");

                        try {
                            Double created_at = (Double) v.getProperty("created_at");
                            if (created_at < minCreated) minCreated = created_at;
                            if (created_at > maxCreated) maxCreated = created_at;
                        } catch (Exception e) {
                            //
                        }
                        fixCount++;
                    }
                }
                keyCount++;
                if (keyCount % 1000 == 0)
                    System.out.println(keyCount + " vertices inspected");
            }
        } catch (StorageException e) {
            throw new RepairException(e);
        }
        itx.commit();

        System.out.println("");
        System.out.println("partial vertex " + (repair?"repair":"check") + " completed");
        System.out.println("  > " + keyCount + " keys examined");
        System.out.println("  > " + fixCount + " partial vertices " + (repair?"removed":"detected"));
        System.out.println("  > min timestamp: " + minCreated);
        System.out.println("  > max timestamp: " + maxCreated);

    }

    public void cleanVertices() throws RepairException {
        cleanVertices(true);
    }

    public void checkVertices() throws RepairException {
        cleanVertices(false);
    }
}
