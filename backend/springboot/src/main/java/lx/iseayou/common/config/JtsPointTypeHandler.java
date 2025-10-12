package lx.iseayou.common.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.postgresql.util.PGobject;

import java.sql.*;

public class JtsPointTypeHandler extends BaseTypeHandler<Point> {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Point parameter, JdbcType jdbcType)
            throws SQLException {
        PGobject geomObj = new PGobject();
        geomObj.setType("geometry");
        geomObj.setValue(String.format("POINT(%f %f)", parameter.getX(), parameter.getY()));
        ps.setObject(i, geomObj);
    }

    @Override
    public Point getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toPoint(rs.getString(columnName));
    }

    @Override
    public Point getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toPoint(rs.getString(columnIndex));
    }

    @Override
    public Point getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toPoint(cs.getString(columnIndex));
    }

    private Point toPoint(String wkt) throws SQLException {
        if (wkt == null) return null;
        try {
            WKTReader reader = new WKTReader(geometryFactory);
            return (Point) reader.read(wkt);
        } catch (Exception e) {
            throw new SQLException("Failed to convert WKT to Point: " + wkt, e);
        }
    }
}
