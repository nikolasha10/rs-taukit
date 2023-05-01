package rs.taukit.element;

@SuppressWarnings("unchecked")
public abstract class AElement implements IElement{
    public <T> T castTo(Class<T> elementType){
        return (T) this;
    }
}
