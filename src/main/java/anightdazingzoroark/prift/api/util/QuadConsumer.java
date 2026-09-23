package anightdazingzoroark.prift.api.util;

//how much more of this shit lol
@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {
    void accept(A a, B b, C c, D d);
}
