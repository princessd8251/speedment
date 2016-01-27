/*
 * Copyright 2016 Speedment, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.speedment.internal.ui.config;

import com.speedment.Speedment;
import com.speedment.exception.SpeedmentException;
import com.speedment.internal.core.stream.OptionalUtil;
import com.speedment.internal.ui.config.trait.HasExpandedProperty;
import com.speedment.internal.ui.config.trait.HasNameProperty;
import com.speedment.stream.MapStream;
import com.speedment.util.OptionalBoolean;
import java.util.Arrays;
import static java.util.Collections.newSetFromMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import static javafx.collections.FXCollections.observableMap;
import static javafx.collections.FXCollections.unmodifiableObservableMap;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import static javafx.collections.FXCollections.observableList;

/**
 *
 * @author        Emil Forslund
 * @param <THIS>  the type of the implementing class
 */
public abstract class AbstractDocumentProperty<THIS extends AbstractDocumentProperty<? super THIS>> 
    implements DocumentProperty, HasExpandedProperty, HasNameProperty {
 
    private final Map<String, Object> config;
    private final transient ObservableMap<String, Property<?>> properties;
    private final transient ObservableMap<String, ObservableList<AbstractDocumentProperty>> children;
    
    /**
     * Invalidation listeners required by the {@code Observable} interface.
     */
    private final transient Set<InvalidationListener> listeners;
    
    protected AbstractDocumentProperty() {

        this.config     = new ConcurrentHashMap<>();
        this.properties = observableMap(new ConcurrentHashMap<>());
        this.children   = observableMap(new ConcurrentHashMap<>());
        this.listeners  = newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public final Map<String, Object> getData() {
        return config;
    }
    
    @Override
    public final void put(String key, Object val) {
        throw new UnsupportedOperationException(
            "Observable config documents does not support the put()-operation " +
            "directly. Instead you should request the appropriate property or " +
            "observable list for the specific key and modify it."
        );
//        
//        if (val instanceof List<?>) {
//            @SuppressWarnings("unchecked")
//            final List<Object> list = (List<Object>) val;
//            
//            for (final Object o : list) {
//                if (o instanceof Map<?, ?>) {
//                    @SuppressWarnings("unchecked")
//                    final Map<String, Object> data = (Map<String, Object>) o;
//                    observableListOf(key).add(createChild(key, data));
//                } else {
//                    throw new SpeedmentException(
//                        "A list that could not be considered a child document was " +
//                        "added to the " + mainInterface().getSimpleName() + "."
//                    );
//                }
//            }
//        } else {
//            @SuppressWarnings("unchecked")
//            final Property<Object> property = (Property<Object>)
//                properties.computeIfAbsent(key, k -> {
//                    final Property<?> prop;
//
//                    if (val instanceof String) {
//                        prop = new SimpleStringProperty();
//                    } else if (val instanceof Boolean) {
//                        prop = new SimpleBooleanProperty();
//                    } else if (val instanceof Integer) {
//                        prop = new SimpleIntegerProperty();
//                    } else if (val instanceof Long) {
//                        prop = new SimpleLongProperty();
//                    } else if (val instanceof Number) {
//                        prop = new SimpleDoubleProperty();
//                    } else {
//                        prop = new SimpleObjectProperty<>();
//                    }
//
//                    @SuppressWarnings("unchecked")
//                    final Property<Object> result = 
//                        (Property<Object>) addListeners(k, prop);
//
//                    return result;
//                });
//
//            property.setValue(val);
//        }
    }

    @Override
    public final Optional<Object> get(String key) {
        final Property<Object> prop = (Property<Object>) properties.get(key);
        if (prop == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(prop.getValue());
        }
    }

    @Override
    public final OptionalBoolean getAsBoolean(String key) {
        final BooleanProperty prop = (BooleanProperty) properties.get(key);
        if (prop == null) {
            return OptionalBoolean.empty();
        } else {
            return OptionalBoolean.ofNullable(prop.getValue());
        }
    }

    @Override
    public final OptionalLong getAsLong(String key) {
        final LongProperty prop = (LongProperty) properties.get(key);
        if (prop == null) {
            return OptionalLong.empty();
        } else {
            return OptionalUtil.ofNullable(prop.getValue());
        }
    }

    @Override
    public final OptionalDouble getAsDouble(String key) {
        final DoubleProperty prop = (DoubleProperty) properties.get(key);
        if (prop == null) {
            return OptionalDouble.empty();
        } else {
            return OptionalUtil.ofNullable(prop.getValue());
        }
    }

    @Override
    public final OptionalInt getAsInt(String key) {
        final IntegerProperty prop = (IntegerProperty) properties.get(key);
        if (prop == null) {
            return OptionalInt.empty();
        } else {
            return OptionalUtil.ofNullable(prop.getValue());
        }
    }

    @Override
    public final Optional<String> getAsString(String key) {
        final StringProperty prop = (StringProperty) properties.get(key);
        if (prop == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(prop.getValue());
        }
    }
    
    @Override
    public final StringProperty stringPropertyOf(String key, Supplier<String> ifEmpty) {
        return (StringProperty) properties.computeIfAbsent(key, k -> addListeners(k, new SimpleStringProperty(ifEmpty.get())));
    }

    @Override
    public final IntegerProperty integerPropertyOf(String key, IntSupplier ifEmpty) {
        return (IntegerProperty) properties.computeIfAbsent(key, k -> addListeners(k, new SimpleIntegerProperty(ifEmpty.getAsInt())));
    }

    @Override
    public final LongProperty longPropertyOf(String key, LongSupplier ifEmpty) {
        return (LongProperty) properties.computeIfAbsent(key, k -> addListeners(k, new SimpleLongProperty(ifEmpty.getAsLong())));
    }

    @Override
    public final DoubleProperty doublePropertyOf(String key, DoubleSupplier ifEmpty) {
        return (DoubleProperty) properties.computeIfAbsent(key, k -> addListeners(k, new SimpleDoubleProperty(ifEmpty.getAsDouble())));
    }

    @Override
    public final BooleanProperty booleanPropertyOf(String key, BooleanSupplier ifEmpty) {
        return (BooleanProperty) properties.computeIfAbsent(key, k -> addListeners(k, new SimpleBooleanProperty(ifEmpty.getAsBoolean())));
    }

    @Override
    public final <T> ObjectProperty<T> objectPropertyOf(String key, Class<T> type, Supplier<T> ifEmpty) throws SpeedmentException {
        @SuppressWarnings("unchecked")
        final ObjectProperty<T> prop = (ObjectProperty<T>) 
            properties.computeIfAbsent(key, k -> addListeners(k, new SimpleObjectProperty<>(ifEmpty.get())));
        
        return prop;
    }

    @Override
    public final <T extends DocumentProperty> ObservableList<T> observableListOf(String key) {
        @SuppressWarnings("unchecked")
        final ObservableList<T> list = (ObservableList<T>)
            children.computeIfAbsent(key, k -> 
                addListeners(k, observableList(new CopyOnWriteArrayList<>()))
            );
        
        return list;
    }
    
    @Override
    public final ObservableMap<String, ObservableList<DocumentProperty>> childrenProperty() {
        return unmodifiableObservableMap(
            (ObservableMap<String, ObservableList<DocumentProperty>>) 
            (ObservableMap<String, ?>) 
            children
        );
    }

    @Override
    public final Stream<? extends DocumentProperty> children() {
        return MapStream.of(children)
            .sortedByKey(Comparator.naturalOrder())
            .flatMapValue(ObservableList::stream)
            .values();
    }

    @Override
    public final void invalidate() {
        listeners.forEach(l -> l.invalidated(this));
        getParent().map(DocumentProperty.class::cast)
            .ifPresent(DocumentProperty::invalidate);
    }

    @Override
    public final void addListener(InvalidationListener listener) {
        listeners.add(listener);
    }

    @Override
    public final void removeListener(InvalidationListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * An overridable method used to get the full key path with the specified 
     * trail. This is used to locate the appropriate constructor.
     * 
     * @param key  the key to end with (can be null)
     * @return     the constructor
     */
    protected abstract String[] keyPathEndingWith(String key);
    
    /**
     * Creates a new child on the specified key with the specified data and 
     * returns it. This method can be overriden by subclasses to create better
     * implementations.
     * <p>
     * <b>Warning!</b> This method is only intended to be called internally and does
     * not properly configure created children in the responsive model.
     * 
     * @param speedment  the speedment instance
     * @param key        the key to create the child on
     * @return           the created child
     */
    protected final AbstractDocumentProperty createChild(Speedment speedment, String key) {
        
        final String path = Arrays.asList(keyPathEndingWith(key)).toString();
        final AbstractDocumentProperty doc = speedment.getDocumentPropertyComponent()
            .getConstructor(keyPathEndingWith(key))
            .create(this);
        
        System.out.println("Path: '" + path + "' resulted in document of type: '" + doc.getClass().getSimpleName() + "'.");
        
        return speedment.getDocumentPropertyComponent()
            .getConstructor(keyPathEndingWith(key))
            .create(this);
    }
    
//    private void loadFrom(ObservableMap<String, ObservableList<AbstractDocumentProperty>> children, Map<String, Object> data) {
//        // Load properties and children for every existing value
//        for (final Map.Entry<String, Object> entry : data.entrySet()) {
//            final String key = entry.getKey();
//            final Object val = entry.getValue();
//            
//            // Check if the specified value could be considered a child.
//            boolean wasChild = false;
//            if (val instanceof List<?>) {
//                @SuppressWarnings("unchecked")
//                final List<Object> list = (List<Object>) val;
//                
//                if (!list.isEmpty()) {
//                    final Object first = list.get(0);
//                    
//                    if (first instanceof Map<?, ?>) {
//                        @SuppressWarnings("unchecked")
//                        final List<Map<String, Object>> castedList = 
//                            (List<Map<String, Object>>) (List<?>) list;
//                        
//                        final ObservableList<AbstractDocumentProperty> docList =
//                            children.computeIfAbsent(key, k -> {
//                                final ObservableList<AbstractDocumentProperty> newList =
//                                    observableList(new CopyOnWriteArrayList<>());
//
//                                return newList;
//                            });
//                        
//                        castedList.stream()
//                            .map(child -> createChild(key, child))
//                            .forEachOrdered(docList::add);
//                        
//                        wasChild = true;
//                    }
//                }
//            }
//            
//            // If the value did not meet the conditions to be considered a
//            // child, consider it a property.
//            if (!wasChild) {
//                @SuppressWarnings("unchecked")
//                final Property<Object> property = (Property<Object>) 
//                    properties.computeIfAbsent(key, k -> {
//                        final Property<?> prop;
//                        if (val instanceof String) {
//                            @SuppressWarnings("unchecked")
//                            final String casted = (String) val;
//                            prop = new SimpleStringProperty(casted);
//                        } else if (val instanceof Boolean) {
//                            @SuppressWarnings("unchecked")
//                            final Boolean casted = (Boolean) val;
//                            prop = new SimpleBooleanProperty(casted);
//                        } else if (val instanceof Integer) {
//                            @SuppressWarnings("unchecked")
//                            final Integer casted = (Integer) val;
//                            prop = new SimpleIntegerProperty(casted);
//                        } else if (val instanceof Long) {
//                            @SuppressWarnings("unchecked")
//                            final Long casted = (Long) val;
//                            prop = new SimpleLongProperty(casted);
//                        } else if (val instanceof Number) {
//                            @SuppressWarnings("unchecked")
//                            final Number casted = (Number) val;
//                            prop = new SimpleDoubleProperty(casted.doubleValue());
//                        } else {
//                            prop = new SimpleObjectProperty<>(val);
//                        }
//
//                        return addListeners(key, prop);
//                    });
//                
//                property.setValue(val);
//            }
//        }
//    }
    
    private <T> Property<T> addListeners(String key, Property<T> property) {
        property.addListener((ob, o, n) -> {
            config.put(key, n);
        });
        
        return property;
    }
    
    private ObservableList<AbstractDocumentProperty> addListeners(String key, ObservableList<AbstractDocumentProperty> list) {
        // When an observable children list under a specific key is
        // modified, the new children must be inserted into the source
        // equivalent as well.
        list.addListener((ListChangeListener.Change<? extends DocumentProperty> listChange) -> {
            while (listChange.next()) {
                if (listChange.wasAdded()) {

                    // Find or create a children list in the source map
                    // for the specified key
                    final List<Map<String, Object>> source = 
                        (List<Map<String, Object>>) config.computeIfAbsent(
                            key, k -> new CopyOnWriteArrayList<>()
                        );

                    // Add a reference to the map for every child
                    listChange.getAddedSubList().stream()
                        .map(DocumentProperty::getData) // Exactly the same map as is used in the property
                        .forEachOrdered(source::add);
                }

//                if (listChange.wasRemoved()) {
//                    throw new SpeedmentException(
//                        "DocumentProperty " + getClass().getSimpleName() + 
//                        " was modified so that list elements on key '" + key +
//                        "' was removed. Removal of list elements are not allowed in this " +
//                        "implementation."
//                    );
//                }
            }
        });
        
        return list;
    }
}