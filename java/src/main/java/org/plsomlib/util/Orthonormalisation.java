package org.plsomlib.util;

import java.util.ArrayList;

import javax.vecmath.GVector;
 
/**
 * @author Erik Berglund
 * 
 */
public class Orthonormalisation
{
    /**
     * Orthonormalise the set of vectors in-place. When this method completes
     * the argument will contain a set of orthonormal vectors.
     * 
     * The vectors are normalised.
     * 
     * 
     * @param vectors
     */
    public static void doGramSchmidt(ArrayList<GVector> vectors)
    {
        for (int j = 0; j < vectors.size(); j++)
        {
            GVector vj = vectors.get(j);
            for (int i = 0; i < j; i++)
            {
                // subtract the projection of vj onto vi.
                GVector proj = proj(vectors.get(i), vj);
                vj.sub(proj);
            }
            vj.normalize();
        }
    }
 
    private static GVector proj(GVector u, GVector v)
    {
        double scaling = v.dot(u) / u.dot(u);
        GVector res = new GVector(u);
        res.scale(scaling);
        return res;
    }
     
    /**
     * Do in-place orthonormalisation of the primary array of vectors.
     * 
     * The same operations will be carried out on the secondary array of vectors.
     * 
     * At the end of this call the primary array will contain an orthonormal set, but the secondary array will not necessarily do so.
     * 
     * Preconditions: primary and secondary must be the same length.
     * 
     * @param primary
     * @param secondary
     */
    public static void doDoubleGramSchmidt(ArrayList<GVector>primary,ArrayList<GVector>secondary)
    {
        if(primary.size()!=secondary.size())
        {
            throw new IllegalArgumentException("primary and secondary arrays must be of same size.");
        }
 
        for (int j = 0; j < primary.size(); j++)
        {
            GVector pvj = primary.get(j);
            GVector svj = secondary.get(j);
            for (int i = 0; i < j; i++)
            {
                // subtract the projection of vj onto vi.
                GVector pvi = primary.get(i);
                GVector svi = secondary.get(i);
                GVector proj = proj(pvi, pvj);
                pvj.sub(proj);
                double scaling =proj.norm()/pvi.norm();
                GVector sec = new GVector(svi);
                sec.scale(scaling);
                svj.sub(sec);
            }
            pvj.normalize();
            svj.normalize();
        }
    }
}