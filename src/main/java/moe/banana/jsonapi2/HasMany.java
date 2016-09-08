package moe.banana.jsonapi2;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;

public final class HasMany<T extends Resource> implements Relationship, Iterable<T>, Serializable {

    public final ResourceLinkage[] linkages;

    private final Class<T> type;
    private final Resource resource;

    HasMany(Class<T> type, Resource resource, ResourceLinkage[] linkages) {
        this.type = type;
        this.resource = resource;
        this.linkages = linkages;
    }

    @Deprecated
    public T[] get() throws ResourceNotFoundException {
        return getAll();
    }

    @SuppressWarnings("unchecked")
    public T[] getAll() throws ResourceNotFoundException {
        T[] array = (T[]) Array.newInstance(type, linkages.length);
        for (int i = 0; i != linkages.length; i++) {
            array[i] = (T) resource._doc.find(linkages[i]);
        }
        return array;
    }

    /**
     * Iterates over linked resources.
     * @return iterator whose {@link Iterator#next()} returns linked Resource or null if linkage cannot be resolved with document.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return linkages != null && i != linkages.length;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                try {
                    return (T) resource._doc.find(linkages[i++]);
                } catch (ResourceNotFoundException e) {
                    return null;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T extends Resource> HasMany<T> create(Resource resource, T... linked) {
        ResourceLinkage[] linkages = new ResourceLinkage[linked.length];
        for (int i = 0; i != linkages.length; i++) {
            linkages[i] = ResourceLinkage.of(linked[i]);
        }
        return create(resource, (Class<T>) linked.getClass().getComponentType(), linkages);
    }

    public static HasMany<? extends Resource> create(Resource resource, ResourceLinkage... linkage) {
        return create(resource, Resource.class, linkage);
    }

    public static <T extends Resource> HasMany<T> create(Resource resource, Class<T> componentType, ResourceLinkage... linkage) {
        return new HasMany<>(componentType, resource, linkage);
    }
}
