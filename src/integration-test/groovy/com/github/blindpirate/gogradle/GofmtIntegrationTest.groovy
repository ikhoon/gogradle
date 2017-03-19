package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class GofmtIntegrationTest extends IntegrationTestSupport {

    String mainDotGo = '''\
package main

import "fmt"

func main() {
fmt.Printf("hello world")
return 0
}
'''
    String subDotGo = '''\
package sub 

import "fmt"

func sub() {
fmt.Printf("hello world")
return 0
}
'''

    String buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath="github.com/my/package"
}
"""

    @Before
    void setUp() {
        IOUtils.write(resource, 'main.go', mainDotGo)
        IOUtils.write(resource, 'sub/sub.go', subDotGo)
        writeBuildAndSettingsDotGradle(buildDotGradle)
    }

    @Test
    void 'gofmt should succeed'() {
        newBuild {
            it.forTasks('fmt')
        }

        assert new File(resource, 'main.go').getText() == '''\
package main

import "fmt"

func main() {
\tfmt.Printf("hello world")
\treturn 0
}
'''
        assert new File(resource, 'sub/sub.go').getText() == '''\
package sub

import "fmt"

func sub() {
\tfmt.Printf("hello world")
\treturn 0
}
'''
    }

    @Test
    void 'customized gofmt should not affect other files'() {
        writeBuildAndSettingsDotGradle(buildDotGradle + '''
fmt {
    doLast {
        gofmt '-w main.go'
    }
}
''')
        newBuild {
            it.forTasks('fmt')
        }

        assert new File(resource, 'sub/sub.go').getText() == subDotGo
    }

    @Override
    File getProjectRoot() {
        return resource
    }

}
