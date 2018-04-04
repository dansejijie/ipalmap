local function GET_FONT_PATH()
    local engine = GetEngine()
    local properties = engine.properties
    local os = properties["os"]
    if os then
        if os >= "iOS/7.0" and os < "iOS/8.0" then
            return "/System/Library/Fonts/Cache/STHeiti-Light.ttc"
        elseif os >= "iOS/8.0" and os < "iOS/9.0" then
            return "/System/Library/Fonts/Core/STHeiti-Light.ttc"
        elseif os >= "iOS/9.0" then
            return "/System/Library/Fonts/LanguageSupport/PingFang.ttc"
        else
            return "/System/Library/Fonts/LanguageSupport/PingFang.ttc"
        end
    else
        return properties["lua_path"] .. "/DroidSansFallback.ttf"
    end
end

-- Android
local function GET_CACHE_PATH()
    local engine = GetEngine()
    local properties = engine.properties

    return properties["cache_folder"]
end

local function NULLSTYLE()
    return {
        ['2d'] = {
            style = 'nullstyle',
        }
    }
end

-- 获取默认图标路径
local function GET_ICON_CACHE_PATH()
    local engine = GetEngine()
    local properties = engine.properties
    local os = properties["OS"]

    if os == "iOS" or os == "iPhone OS" then
        local icon_path = properties["iconCachePath"]
        return icon_path
    elseif os == "Android" then
        local path = properties["cache_folder"]
        local icon_path = path .. "/icon/"
        return icon_path
    else
        return ""
    end
end

local function DEFAULT_STYLE()
    return {
        ['2d'] = {
            style = 'polygon',
            face = {
                color = '0xffd1c3b1',
                enable_alpha = false,
                texture = null,
                automatic_scale = null
            },
            outline = {
                color = '0xffc0aa94',
                width = 1,
                enable_alpha = false,
                alignment = 'AlignRight',
            }
        },
        ['3d'] = {
            style = 'polygon',
            face_on_bottom = false,
            height = 1.5,
            face = {
                color = '0xffd1c3b1',
                enable_alpha = false,
            },
            outline = {
                color = '0xffc0aa94',
                width = 1,
                height = 1.5,
                enable_alpha = false,
                enable_edge_shadow = true,
                alignment = 'AlignRight',
            },
        }
    }
end


-- 3D样式POI
local function SetPolygonStyle_3D(facecolor, styleheight, linecolor, linewidth, face_on_bottom, alpha, align)
    return {
        ['2d'] = {
            style = 'polygon',
            face = {
                enable_alpha = alpha or false,
                color = facecolor or '0xFFF9F5BA',
            },
            outline = {
                color = linecolor or '0xFF999999',
                width = linewidth or 0.05,
                enable_alpha = alpha or false,
                alignment = align or 'AlignRight',
            },
        },
        ['3d'] = {
            style = 'polygon',
            face_on_bottom = face_on_bottom or false, --为false时 height才有效
            height = styleheight or 3,
            face = {
                color = facecolor or '0xFFF9F5BA',
                enable_alpha = alpha or false,
            },
            outline = {
                color = linecolor or '0xFF999999',
                width = linewidth or 0.05,
                height = styleheight or 3,
                enable_alpha = alpha or false,
                enable_edge_shadow = true,
                alignment = align or 'AlignRight',
            },
        },
    }
end

CONFIG = {
    views = {
        default = {
            back_color = '0xFFefe6f3',
            layers = {
                Frame = {
                    height_offset = 0.1,
                    renderer = {
                        type = 'simple',
                        ['2d'] = {
                            style = 'polygon',
                            face = {
                                color = '0xeefffff3', --路
                                enable_alpha = true,
                            },
                            outline = {
                                --color = '0xff000000',
                                color = '0xff62619a', --外部边框
                                width = 0,
                                enable_alpha = false,
                                alignment = 'AlignLeft', -- 多边形外框线对齐方式设置, 取值为:'AlignLeft'、'AlignCenter'、'AlignRight',沿顺时针方向分别表示居左(外)、居中、居右(内)对齐
                            },
                            left_side = {}
                        },
                    }
                },
                Area = {
                    height_offset = 0,
                    renderer = {
                        type = 'unique',
                        key = {
                            'id',
                            'category',
                            'display'
                        },
                        default = DEFAULT_STYLE(),
                        styles = {
                            --SetPolygonStyle_3D(facecolor, styleheight, linecolor, linewidth, face_on_bottom, alpha, align)

                            [24001000] = SetPolygonStyle_3D('0xffe8c7a4', 0.7, '0xffc7935b'),
                            [15017000] = SetPolygonStyle_3D('0xffa7e1c4', 0.7, '0xff76d0a3'),
                            [21066000] = SetPolygonStyle_3D('0xffffaaa6', 0.7, '0xffffaaa6'),
                            [2563491] = SetPolygonStyle_3D('0xffffffff', 0.1, '0xffffffff'),
                            [2563447] = SetPolygonStyle_3D('0xffffffff', 0.1, '0xffffffff'),
                            [35002000] = SetPolygonStyle_3D('0xffa7e1c4', 0.1, '0xffa7e1c4'),
                            [24002000] = SetPolygonStyle_3D('0xffadd8f7', 1.5, '0xff7ec2f3'),
                            [24005000] = SetPolygonStyle_3D('0xffdfc27d', 1, '0xffbf812d'),
                            [22001000] = SetPolygonStyle_3D('0xffDEC181', 0.01, '0xffffffff'),
                            [22004000] = SetPolygonStyle_3D('0xffDEC181', 0.01, '0xffffffff'),
                            [15000000] = SetPolygonStyle_3D('0xffa7e1c4', 1.5, '0xff76d0a3'),
                            [2175126] = SetPolygonStyle_3D('0xfffcb8d3', 1.5, '0xfffa90ba'),
                            [2175127] = SetPolygonStyle_3D('0xffa7e1c4', 1.5, '0xff76d0a3'),
                            [2175125] = SetPolygonStyle_3D('0xffa7e1c4', 1.5, '0xff76d0a3'),
                            [2175128] = SetPolygonStyle_3D('0xffcfcaf6', 1.5, '0xffb3acf2'),
                            [12010000] = SetPolygonStyle_3D('0xfffdd8e7', 0.3, '0xfffcb8d3'),
                            [23038000] = SetPolygonStyle_3D('0xfffde3cf', 0.5, '0xff91c8fe'),
                            [23003000] = SetPolygonStyle_3D('0xffffffff', 0.1, '0xffcccccc'),
                            [2508473] = SetPolygonStyle_3D('0xfff46e65', 1.5, '0xfff04134'),
                            [17004000] = SetPolygonStyle_3D('0xffffe9a7', 0.7, '0xffffce3d'),
                            [15011000] = SetPolygonStyle_3D('0xffa7dfe3', 1.5, '0xff76cdd3'),
                            [22072000] = SetPolygonStyle_3D('0xffa7dfe3', 1.5, '0xff76cdd3'),
                            [17009000] = SetPolygonStyle_3D('0xfffcb8d3', 1.5, '0xfffa90ba'),
                            [17002000] = SetPolygonStyle_3D('0xfffcb8d3', 1.5, '0xfffa90ba'),
                            [22095000] = SetPolygonStyle_3D('0xfffcb8d3', 1.5, '0xfffa90ba'),
                            [23010000] = SetPolygonStyle_3D('0xfffcb8d3', 1.5, '0xfffa90ba'),
                            ['纺织服装周刊'] = SetPolygonStyle_3D('0xfffabeb9', 1.5, '0xfff79992'),
                            [23041000] = SetPolygonStyle_3D('0xff91c8fe', 1.5, '0xff91c8fe'),
                            [23054000] = SetPolygonStyle_3D('0xff91c8fe', 0.5, '0xfffaaf76'),
                            [23004000] = SetPolygonStyle_3D('0xff91c8fe', 0.5, '0xff91c8fe'),
                            [22001000] = SetPolygonStyle_3D('0xffFFEBAF', 0.3, '0xffcccccc'),
                            [21003000] = SetPolygonStyle_3D('0xff91c8fe', 1.5, '0xffc0aa94'),
                            [16003000] = SetPolygonStyle_3D('0xff91c8fe', 1.5, '0xffc0aa94'),
                            [23043000] = SetPolygonStyle_3D('0xffcfefdf', 0.2, '0xffcfefdf'),
                            [21047000] = SetPolygonStyle_3D('0xffdee8dc', 1.5, '0xffc0aa94'),
                            [23006000] = SetPolygonStyle_3D('0xffffffff', 1.5, '0xffcccccc'),
                            [23018000] = SetPolygonStyle_3D('0xfff4f3fd', 1.5, '0xffc0aa94'),
                            [23999000] = SetPolygonStyle_3D('0xffe9e9e9', 1.5, '0xffd9d9d9'),
                            [23062000] = SetPolygonStyle_3D('0xffdeebf7', 0.1, '0xff666666'),
                            [23024000] = SetPolygonStyle_3D('0xffcfcaf6', 1.5, '0xffb3acf2'),
                            [23025000] = SetPolygonStyle_3D('0xffcfcaf6', 1.5, '0xffb3acf2'),
                            [23059000] = SetPolygonStyle_3D('0xffcfcaf6', 1.5, '0xffb3acf2'),
                            [23063000] = SetPolygonStyle_3D('0xffcfcaf6', 1.5, '0xffb3acf2'),
                            [24091000] = SetPolygonStyle_3D('0xffffe9a7', 1.5, '0xffffdd76'),
                            [24097000] = SetPolygonStyle_3D('0xffffe9a7', 1.5, '0xffffdd76'),
                            [24092000] = SetPolygonStyle_3D('0xffffe9a7', 1.5, '0xffffdd76'),
                            [24098000] = SetPolygonStyle_3D('0xffffe9a7', 1.5, '0xffffdd76'),
                            [24093000] = SetPolygonStyle_3D('0xffffe9a7', 0.3, '0xffffdd76'),
                            [24094000] = SetPolygonStyle_3D('0xffffe9a7', 0.3, '0xffffdd76'),
                            [24095000] = SetPolygonStyle_3D('0xffffe9a7', 0.3, '0xffffdd76'),
                            [24096000] = SetPolygonStyle_3D('0xffffe9a7', 0.3, '0xffffdd76'),
                            [12008000] = SetPolygonStyle_3D('0xfffabeb9', 0.1, '0xfff79992'),
                            [23019000] = SetPolygonStyle_3D('0xfffabeb9', 0.1, '0xfff79992'),
                            [21008000] = SetPolygonStyle_3D('0xfffabeb9', 1.5, '0xfff79992'),
                            [21005000] = SetPolygonStyle_3D('0xfffabeb9', 1.5, '0xfff79992'),
                            [21049000] = SetPolygonStyle_3D('0xfffabeb9', 1.5, '0xfff79992'),
                            [35003000] = SetPolygonStyle_3D('0xffcbe6a3', 1.5, '0xffc0aa94'),
                            [37000000] = SetPolygonStyle_3D('0xffffffff', 0.4, '0xffffffff'),
                        }
                    }
                },
                Area_text = {
                    collision_detection = true,
                    font_path = GET_FONT_PATH(),
                    renderer = {
                        type = 'simple',
                        ['2d'] = {
                            style = 'annotation',
                            color = '0xFF0f1923',
                            field = 'name',
                            size = 25,
                            outline_color = '0xFFffffff',
                            outline_width = 0.5,
                            anchor_x = 0.5,
                            anchor_y = 0.5,
                            height = 0.1,
                            enable_fadein = true, -- 开启显隐效果
                            aabbox_extend = 5, -- 改变字体间隙
                        },
                    }
                },
                -- 公共设施层
                Facility = {
                    height_offset = -0.2;
                    collision_detection = true,
                    renderer = {
                        type = 'unique',
                        key = {
                            'id',
                            'category',
                        },
                        default = {
                            ['2d'] = {
                                style = 'icon',
                                -- icon = "icon.png", -- 只要配置了当前属性，就加载本地图片
                                icon_url = 'http://api.ipalmap.com/logo/64/',
                                icon_cache = GET_ICON_CACHE_PATH(),
                                icon_online = 'logo',
                                anchor_x = 0.5,
                                anchor_y = 0.5,
                                use_texture_origin_size = false,
                                unit = 'pt', -- 图标大小(width、height)使用的单位,"px"表示像素,"pt"表示1/72英寸
                                width = 7,
                                height = 7,
                                enable_fadein = false, -- 开启显隐效果
                                -- level = 1,
                            },
                        },
                        tyles = {},
                        updatesstyles = {},
                    },
                }, -- End Facility,
                positioning = {
                    height_offset = -0.4,
                    renderer = {
                        type = 'simple',
                        ['2d'] = {
                            style = 'icon',
                            icon = 'icons/location_indoor.png',
                            -- enable_alpha = true,
                            use_texture_origin_size = true
                        }
                    }
                },
                naviFireHydrant = {
                    height_offset = -0.4,
                    collision_detection = true,
                    renderer = {
                        type = 'simple',
                        --default = Icon('icons/ic_pin_xfs.png'),
                        ['2d'] = {
                            style = 'icon',
                            icon = 'icons/ic_pin_xfs.png', -- 只要配置了当前属性，就加载本地图片
                            anchor_x = 0.5,
                            anchor_y = 0.5,
                            use_texture_origin_size = false,
                            unit = 'pt', -- 图标大小(width、height)使用的单位,"px"表示像素,"pt"表示1/72英寸
                            width = 17.5,
                            height = 20,
                            enable_fadein = false, -- 显隐效果,如果不需要碰撞检测到话，关闭
                            level = 1,
                        }
                    }
                },
                lineLayer = {
                    -- 导航图层参考样式设置
                    height_offset = -1.2,
                    renderer = {
                        type = 'simple',
                        ['2d'] = {
                            style = 'linestring',
                            color = '0xFF97e0bd', -- 颜色
                            -- color = '0xFF0000FF', -- 颜色
                            -- color = '0xFF009bff', -- 颜色
                            width = 0.8, -- 线宽
                            enable_alpha = true,
                            automatic_scale = true,
                        },
                    }
                },
                navigate = {
                    -- 导航图层参考样式设置
                    height_offset = -1.2,
                    renderer = {
                        type = 'simple',
                        ['2d'] = {
                            style = 'linestring',
                            color = '0xFF97E0BD', -- 颜色
                            width = 1.5, -- 线宽
                            line_style = 'NONE', -- 线型，NONE、ARROW、DASHED
                            has_arrow = true, -- 是否绘制方向指示箭头，仅在line_style为NONE时有效
                            has_start = true, -- 绘制起始点
                            has_end = true, -- 绘制终点
                            enable_alpha = true,
                            automatic_scale = true,
                        },
                    }
                },
            }
        },
    }
}